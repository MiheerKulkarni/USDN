package com.github.usdn.flowtable;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class FlowtableEntry implements FlowTableInterface {
    private final List<FlowtableStructure> Entry = new LinkedList<>();

    private final List<AbstractAction> actions = new LinkedList<>();
    private Stats stats = new Stats();

    public static FlowtableEntry fromString(final String s) {
        String val = s.toUpperCase();
        FlowtableEntry res = new FlowtableEntry();

        String[] strWindows = (val.substring(
                val.indexOf("(") + 1, val.indexOf(")"))).split("&&");

        for (String w : strWindows) {
            res.addWindow(FlowtableStructure.fromString(w.trim()));
        }

        String[] strActions = (val.substring(
                val.indexOf("{") + 1, val.indexOf("}"))).trim().split(";");

        for (String a : strActions) {
            res.addAction(ActionBuilder.build(a.trim()));
        }
        return res;
    }

    public FlowtableEntry() {
    }
    public FlowtableEntry(final byte[] entry) {
        int i = 0;

        int nWindows = entry[i];

        for (i = 1; i <= nWindows; i += FlowtableStructure.SIZE) {
            Entry.add(new FlowtableStructure(
                    Arrays.copyOfRange(entry, i, i + FlowtableStructure.SIZE)));
        }

        while (i < entry.length - (Stats.SIZE)) {
            int len = entry[i++];
            actions.add(ActionBuilder.build(
                    Arrays.copyOfRange(entry, i, i + len)));
            i += len;
        }

        stats = new Stats(
                Arrays.copyOfRange(
                        entry, entry.length - Stats.SIZE, entry.length)
        );

    }
    @Override
    public String toString() {
        StringBuilder out = new StringBuilder("if (");

        Entry.stream().map((FlowtableStructure w) -> {
            StringBuilder part = new StringBuilder();
            part.append(w.toString());
            return part;
        }).filter((part) -> (!part.toString().isEmpty())).forEach((part) -> {
            if (out.toString().equals("if (")) {
                out.append(part);
            } else {
                out.append(" && ").append(part);
            }
        });
        if (!out.toString().isEmpty()) {
            out.append(") { ");
            actions.stream().forEach((a) -> {
                out.append(a.toString()).append("; ");
            });
            out.append("} (")
                    .append(getStats().toString())
                    .append(')');
        }
        return out.toString().toUpperCase();
    }

    public List<FlowtableStructure> getEntry() {
        return Entry;
    }
    public void setEntry(final List<FlowtableStructure> w) throws InterruptedException {

        Thread.sleep(1000);
        Entry.clear();
        Entry.addAll(w);
    }
    public boolean addWindow(final FlowtableStructure w) {
        return Entry.add(w);
    }
    public List<AbstractAction> getActions() {
        return actions;
    }
    public void setAction(final List<AbstractAction> a) {
        actions.clear();
        actions.addAll(a);
    }

    public boolean addAction(final AbstractAction a) {
        return actions.add(a);
    }
    public Stats getStats() {
        return stats;
    }

    public void setStats(final Stats s) {
        stats = s;
    }
    @Override
    public byte[] toByteArray() {
        int size = (1 + Entry.size() * FlowtableStructure.SIZE) + Stats.SIZE;
        for (AbstractAction a : actions) {
            size = size + a.getActionLength() + 1;
        }

        ByteBuffer target = ByteBuffer.allocate(size);
        target.put((byte) (Entry.size() * FlowtableStructure.SIZE));

        Entry.stream().forEach((fw) -> {
            target.put(fw.toByteArray());
        });

        actions.stream().map((a) -> {
            target.put((byte) a.getActionLength());
            return a;
        }).forEach((a) -> {
            target.put(a.toByteArray());
        });

        target.put(stats.toByteArray());

        return target.array();
    }
    @Override
    public int hashCode() {
        int hash = Objects.hashCode(Entry) + Objects.hashCode(actions)
                + Objects.hashCode(stats);
        return hash;
    }
    public boolean equalWindows(final FlowtableEntry other) {
        return Objects.deepEquals(Entry, other.Entry);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FlowtableEntry other = (FlowtableEntry) obj;
        if (!Objects.deepEquals(Entry, other.Entry)) {
            return false;
        }
        return Objects.deepEquals(actions, other.actions);
    }





}
