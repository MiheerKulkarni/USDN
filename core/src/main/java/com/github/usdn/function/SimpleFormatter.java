package com.github.usdn.function;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class SimpleFormatter extends Formatter {

    private final SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");

    private final String name;

    public SimpleFormatter(final String n) {
        name = n;
    }
    @Override
    public synchronized String format(LogRecord record) {

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
            }
        }
        return sb.append("\n").toString();
    }
}
