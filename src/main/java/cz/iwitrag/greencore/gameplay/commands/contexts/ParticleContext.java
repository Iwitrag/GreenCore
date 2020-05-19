package cz.iwitrag.greencore.gameplay.commands.contexts;

import co.aikar.commands.InvalidCommandArgument;
import org.bukkit.Particle;

import java.util.ArrayList;
import java.util.List;

public class ParticleContext extends AbstractContext {
    @Override
    public void registerCommandContext() {
        paperCommandManager.getCommandCompletions().registerAsyncCompletion("particles", c -> {
            List<String> completions = new ArrayList<>();
            for (Particle particle : Particle.values()) {
                completions.add(particle.name());
            }
            return completions;
        });

        paperCommandManager.getCommandContexts().registerContext(Particle.class, c -> {
            String arg = c.popFirstArg();
            for (Particle particle : Particle.values()) {
                if (particle.name().equalsIgnoreCase(arg))
                    return particle;
            }
            throw new InvalidCommandArgument("§cNeplatný typ částice", false);
        });
    }
}
