package cz.iwitrag.greencore.gameplay.playerskills;

import cz.iwitrag.greencore.playerbase.GPlayer;

public class SkillStatus {

    private GPlayer player;
    private Skill skill;
    private int level;
    private double experience;

    public SkillStatus(GPlayer player, Skill skill) {
        this(player, skill, 1, 0.00);
    }

    public SkillStatus(GPlayer player, Skill skill, int level, double experience) {
        this.player = player;
        this.skill = skill;
        this.level = level;
        this.experience = experience;
    }

    public SkillStatus(SkillStatus skillStatus) {
        this.player = skillStatus.player;
        this.skill = skillStatus.skill;
        this.level = skillStatus.level;
        this.experience = skillStatus.experience;
    }

    public GPlayer getPlayer() {
        return player;
    }

    public Skill getSkill() {
        return skill;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public double getExperience() {
        return experience;
    }

    public void setExperience(double experience) {
        this.experience = experience;
    }
}
