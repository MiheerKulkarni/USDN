package com.github.usdn.motes.logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class MoteFormatter extends Formatter {
    @Override
    public final synchronized String format(final LogRecord record) {

        StringBuilder sb = new StringBuilder();
        String message = formatMessage(record);

        sb.append(message);

        if (record.getThrown() != null) {
            try {
                StringWriter sw = new StringWriter();
                try (PrintWriter pw = new PrintWriter(sw)) {
                    record.getThrown().printStackTrace(pw);
                }
                sb.append(sw.toString());
            } catch (Exception ex) {
                System.err.println(ex.getLocalizedMessage());
            }
        }
        return sb.append("\n").toString();
    }
}
