package cz.iwitrag.greencore.playerbase;

import cz.iwitrag.greencore.gameplay.playerskills.Skill;
import cz.iwitrag.greencore.gameplay.playerskills.SkillStatus;
import cz.iwitrag.greencore.gameplay.playerskills.SkillsExpTable;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class GPlayer {

    private int id;
    private Player bukkitPlayer;
    private Map<Skill, SkillStatus> skills = new HashMap<>();

    // TODO - persistent player skills in DB

    public GPlayer(Player bukkitPlayer) {
        this.bukkitPlayer = bukkitPlayer;
        for (Skill skill : Skill.values())
            skills.put(skill, new SkillStatus(this, skill));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Player getBukkitPlayer() {
        return bukkitPlayer;
    }

    public void setBukkitPlayer(Player bukkitPlayer) {
        this.bukkitPlayer = bukkitPlayer;
    }

    public Map<Skill, SkillStatus> getSkills() {
        return skills;
    }

    public void setSkills(Map<Skill, SkillStatus> skills) {
        this.skills = skills;
    }

    public void addSkillExperience(Skill skill, double amount) {
        SkillStatus skillStatus = skills.get(skill);
        int currentLevel = skillStatus.getLevel();
        double currentExp = skillStatus.getExperience();

        skillStatus.setExperience(currentExp + amount);

        while (SkillsExpTable.nextLevelReached(currentLevel, currentExp)) {
            int newLevel = currentLevel+1;
            double newExp = currentExp-SkillsExpTable.getExpForNextLevel(currentLevel);
            skillStatus.setLevel(newLevel);
            skillStatus.setExperience(newExp);
            currentLevel = newLevel;
            currentExp = newExp;
            bukkitPlayer.sendMessage("§aTvůj level v §l" + skill + "§a se zvýšil na §l" + newLevel + "§a!");
        }
    }

    public SkillStatus getSkillStatus(Skill skill) {
        return new SkillStatus(skills.get(skill));
    }

}
