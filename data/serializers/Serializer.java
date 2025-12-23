package daripher.skilltree.data.serializers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public interface Serializer<T> {
   T deserialize(JsonObject var1) throws JsonParseException;

   void serialize(JsonObject var1, T var2);

   T deserialize(CompoundTag var1);

   CompoundTag serialize(T var1);

   T deserialize(FriendlyByteBuf var1);

   void serialize(FriendlyByteBuf var1, T var2);
}
