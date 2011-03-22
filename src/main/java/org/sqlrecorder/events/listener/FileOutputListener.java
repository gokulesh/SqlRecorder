package org.sqlrecorder.events.listener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.sqlrecorder.config.RuntimeResultConfiguration;
import org.sqlrecorder.events.filter.AllowAllSqlFilter;
import org.sqlrecorder.events.filter.SqlOutputFilter;
import org.sqlrecorder.exception.SQLRecorderException;
import org.sqlrecorder.util.LogUtils;

public final class FileOutputListener extends BaseStatementListener {

    private static final Logger LOG = LogUtils.loggerForThisClass();
    private static final String LISTENER_ID = "file-logger";
    private static final String NEWLINE = "\n";

    private final File file;
    private final OutputStreamWriter fileWriter;

    public FileOutputListener(String fileName) {
        this(fileName, new AllowAllSqlFilter());
    }

    public FileOutputListener(String fileName, SqlOutputFilter... sqlOutputFilters) {
        super(sqlOutputFilters);

        Preconditions.checkArgument(StringUtils.isNotBlank(fileName), "File name path cannot be empty for listener");
        file = createAndValidateFile(fileName);
        try {
            fileWriter = new OutputStreamWriter(new FileOutputStream(file,true), Charset.forName("UTF-8"));
        } catch (IOException e) {
            throw new SQLRecorderException("Unable to create File writer for listener: " + LISTENER_ID, e);
        }
    }

    public String id() {
        return LISTENER_ID;
    }

    public boolean returnsExecutedQueries() {
        return false;
    }

    public void shutDown() {
        try {
            fileWriter.close();
            LOG.info("Closed file: " + file.getAbsolutePath());
        } catch (IOException e) {
            LOG.error("Unable to close writer for file: " + file.getAbsolutePath(), e);
        }
    }

    public List<String> executedQueries() {
        throw new SQLRecorderException("Does not return the list of queries executed");
    }

    @Override
    protected void processEvent(String sql) {

        try {
            String requestId = RuntimeResultConfiguration.getCurrentFunctionalRequest();
            StringBuilder sb = new StringBuilder(new Date().toString());
            requestId = createRequestId(requestId);
            sb.append(requestId).append(sql).append(NEWLINE);

            //TODO Change this from synchronized to another queue/actor
            // based mechanism ? See if it is faster.
            synchronized (fileWriter) {
                fileWriter.append(sb.toString());
                fileWriter.flush();
            }
        } catch (IOException e) {
            throw new SQLRecorderException("Unable to append to file " + file.getAbsolutePath(), e);
        }
    }

    private File createAndValidateFile(String fileName) {
        File f = new File(fileName);
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                throw new SQLRecorderException("Unable to create file: " + fileName, e);
            }
        } else if (!f.isFile()) {
            throw new SQLRecorderException("Specified file is not a regular file. Is it a dir. ? " + fileName);
        }
        return f;
    }

    private String createRequestId(String requestId) {
        if (StringUtils.isNotBlank(requestId)) {
            requestId = " [" + requestId + "]:";
        } else {
            requestId = " []";
        }
        return requestId;
    }
}
