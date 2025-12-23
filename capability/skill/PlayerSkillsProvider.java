package daripher.skilltree.capability.skill;

import daripher.skilltree.network.NetworkDispatcher;
import daripher.skilltree.network.message.SyncPlayerSkillsMessage;
import daripher.skilltree.network.message.SyncServerDataMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.Clone;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(
   modid = "skilltree"
)
public class PlayerSkillsProvider implements ICapabilitySerializable<CompoundTag> {
   private static final ResourceLocation CAPABILITY_ID = new ResourceLocation("skilltree", "player_skills");
   private static final Capability<IPlayerSkills> CAPABILITY = CapabilityManager.get(new CapabilityToken<IPlayerSkills>() {
   });
   private final LazyOptional<IPlayerSkills> optionalCapability = LazyOptional.of(PlayerSkills::new);

   @SubscribeEvent
   public static void attachCapability(AttachCapabilitiesEvent<Entity> event) {
      if (event.getObject() instanceof Player) {
         PlayerSkillsProvider provider = new PlayerSkillsProvider();
         event.addCapability(CAPABILITY_ID, provider);
      }
   }

   @SubscribeEvent
   public static void persistThroughDeath(Clone event) {
      if (!event.getEntity().level().isClientSide) {
         event.getOriginal().reviveCaps();
         IPlayerSkills originalData = get(event.getOriginal());
         IPlayerSkills cloneData = get(event.getEntity());
         cloneData.deserializeNBT((CompoundTag)originalData.serializeNBT());
         event.getOriginal().invalidateCaps();
      }
   }

   @SubscribeEvent
   public static void syncSkills(PlayerLoggedInEvent event) {
      if (!event.getEntity().level().isClientSide) {
         NetworkDispatcher.network_channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)event.getEntity()), new SyncServerDataMessage());
      }
   }

   @SubscribeEvent(
      priority = EventPriority.LOWEST
   )
   public static void restoreSkillsAttributeModifiers(EntityJoinLevelEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         get(player).getPlayerSkills().forEach(skill -> skill.learn(player, false));
      }
   }

   @SubscribeEvent
   public static void sendTreeResetMessage(EntityJoinLevelEvent event) {
      if (event.getEntity() instanceof Player player) {
         if (!event.getEntity().level().isClientSide) {
            IPlayerSkills capability = get(player);
            if (capability.isTreeReset()) {
               player.sendSystemMessage(Component.translatable("skilltree.message.reset").withStyle(ChatFormatting.YELLOW));
               capability.setTreeReset(false);
            }
         }
      }
   }

   @SubscribeEvent
   public static void syncPlayerSkills(EntityJoinLevelEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         NetworkDispatcher.network_channel.send(PacketDistributor.PLAYER.with(() -> player), new SyncPlayerSkillsMessage(player));
      }
   }

   @NotNull
   public static IPlayerSkills get(Player player) {
      return (IPlayerSkills)player.getCapability(CAPABILITY).orElseThrow(NullPointerException::new);
   }

   public static boolean hasSkills(@NotNull Player player) {
      return player.getCapability(CAPABILITY).isPresent();
   }

   @NotNull
   public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
      return cap == CAPABILITY ? this.optionalCapability.cast() : LazyOptional.empty();
   }

   public CompoundTag serializeNBT() {
      return (CompoundTag)((IPlayerSkills)this.optionalCapability.orElseThrow(NullPointerException::new)).serializeNBT();
   }

   public void deserializeNBT(CompoundTag compoundTag) {
      ((IPlayerSkills)this.optionalCapability.orElseThrow(NullPointerException::new)).deserializeNBT(compoundTag);
   }
}
