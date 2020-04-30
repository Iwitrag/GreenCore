package cz.iwitrag.greencore.gameplay.zones.flags;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

@Entity
@DiscriminatorValue("cmd")
public class BlockedCommandsFlag extends Flag {

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="cmdsflag_commands", joinColumns=@JoinColumn(name="cmdsFlag_id"))
    @Column(name="command")
    private Set<String> blockedCmds = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    public Set<String> getCommands() {
        Set<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.addAll(blockedCmds);
        return result;
    }

    public void setCommands(Set<String> commands) {
        Set<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (String str : commands) {
            result.add(prependSlashIfMissing(str));
        }
        blockedCmds = result;
    }

    public void addCommand(String command) {
        blockedCmds.add(prependSlashIfMissing(command));
    }

    public void addCommands(String... commands) {
        for (int i = 0; i < commands.length; i++) {
            commands[i] = prependSlashIfMissing(commands[i]);
        }
        blockedCmds.addAll(Arrays.asList(commands));
    }

    public void removeCommand(String command) {
        blockedCmds.remove(prependSlashIfMissing(command));
    }

    public void removeCommands(String... commands) {
        for (int i = 0; i < commands.length; i++) {
            commands[i] = prependSlashIfMissing(commands[i]);
        }
        blockedCmds.removeAll(Arrays.asList(commands));
    }

    public void purgeCommands() {
        blockedCmds.clear();
    }

    public boolean hasCommand(String command) {
        String checker = prependSlashIfMissing(command);
        return blockedCmds.stream().anyMatch((cmd) -> cmd.equalsIgnoreCase(checker));
    }

    private String prependSlashIfMissing(String str) {
        return (str.charAt(0) != '/') ? ("/" + str) : str;
    }

    @Override
    public Flag copy() {
        BlockedCommandsFlag flag = new BlockedCommandsFlag();
        flag.setCommands(blockedCmds);
        return flag;
    }
}
