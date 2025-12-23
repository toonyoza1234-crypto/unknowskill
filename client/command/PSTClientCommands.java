package daripher.skilltree.client.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import daripher.skilltree.client.data.SkillTreeEditorData;
import daripher.skilltree.client.screen.SkillTreeEditorScreen;
import daripher.skilltree.data.reloader.SkillTreesReloader;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(
   modid = "skilltree",
   value = {Dist.CLIENT}
)
public class PSTClientCommands {
   public static final SuggestionProvider<CommandSourceStack> SKILL_TREE_ID_PROVIDER = (ctx, builder) -> SharedSuggestionProvider.suggest(
         gatherSkillTreesPaths(), builder
      );
   private static ResourceLocation tree_to_display;
   private static int timer;

   @NotNull
   private static Stream<String> gatherSkillTreesPaths() {
      return Stream.concat(SkillTreesReloader.getSkillTrees().keySet().stream(), SkillTreeEditorData.getEditorTreesIDs().stream())
         .map(ResourceLocation::toString);
   }

   @SubscribeEvent
   public static void registerCommands(RegisterClientCommandsEvent event) {
      LiteralArgumentBuilder<CommandSourceStack> editorCommand = (LiteralArgumentBuilder<CommandSourceStack>)Commands.literal("skilltree")
         .then(
            Commands.literal("editor")
               .then(
                  Commands.argument("treeId", StringArgumentType.greedyString())
                     .suggests(SKILL_TREE_ID_PROVIDER)
                     .executes(PSTClientCommands::displaySkillTreeEditor)
               )
         );
      event.getDispatcher().register(editorCommand);
   }

   @SubscribeEvent
   public static void delayedCommandExecution(ClientTickEvent event) {
      if (timer > 0) {
         timer--;
      } else {
         if (tree_to_display != null) {
            Minecraft.getInstance().setScreen(new SkillTreeEditorScreen(tree_to_display));
            tree_to_display = null;
         }
      }
   }

   private static int displaySkillTreeEditor(CommandContext<CommandSourceStack> ctx) {
      String treeIdArg = ((String)ctx.getArgument("treeId", String.class)).toLowerCase();
      tree_to_display = new ResourceLocation(treeIdArg);
      timer = 1;
      return 1;
   }
}
