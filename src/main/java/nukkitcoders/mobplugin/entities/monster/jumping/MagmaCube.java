package nukkitcoders.mobplugin.entities.monster.jumping;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.entity.CreatureSpawnEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import nukkitcoders.mobplugin.entities.monster.JumpingMonster;
import nukkitcoders.mobplugin.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MagmaCube extends JumpingMonster {

    public static final int NETWORK_ID = 42;

    public static final int SIZE_SMALL = 1;
    public static final int SIZE_MEDIUM = 2;
    public static final int SIZE_BIG = 3;

    protected int size = SIZE_BIG;

    public MagmaCube(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 0.51f + size * 0.51f;
    }

    @Override
    public float getHeight() {
        return 0.51f + size * 0.51f;
    }

    @Override
    public float getLength() {
        return 0.51f + size * 0.51f;
    }

    @Override
    protected void initEntity() {
        super.initEntity();

        this.fireProof = true;

        if (this.namedTag.contains("Size")) {
            this.size = this.namedTag.getInt("Size");
        } else {
            this.size = Utils.rand(1, 3);
        }

        this.setScale(0.51f + size * 0.51f);

        if (size == SIZE_BIG) {
            this.setMaxHealth(16);
        } else if (size == SIZE_MEDIUM) {
            this.setMaxHealth(4);
        } else if (size == SIZE_SMALL) {
            this.setMaxHealth(1);
        }

        if (size == SIZE_BIG) {
            this.setDamage(new float[] { 0, 3, 4, 6 });
        } else if (size == SIZE_MEDIUM) {
            this.setDamage(new float[] { 0, 2, 2, 3 });
        } else {
            this.setDamage(new float[] { 0, 0, 0, 0 });
        }
    }

    @Override
    public void saveNBT() {
        super.saveNBT();

        this.namedTag.putInt("Size", this.size);
    }

    public void attackEntity(Entity player) {
        if (this.attackDelay > 23 && this.distanceSquared(player) < 1) {
            this.attackDelay = 0;
            HashMap<EntityDamageEvent.DamageModifier, Float> damage = new HashMap<>();
            damage.put(EntityDamageEvent.DamageModifier.BASE, this.getDamage());

            if (player instanceof Player) {
                @SuppressWarnings("serial")
                HashMap<Integer, Float> armorValues = new HashMap<Integer, Float>() {

                    {
                        put(Item.LEATHER_CAP, 1f);
                        put(Item.LEATHER_TUNIC, 3f);
                        put(Item.LEATHER_PANTS, 2f);
                        put(Item.LEATHER_BOOTS, 1f);
                        put(Item.CHAIN_HELMET, 1f);
                        put(Item.CHAIN_CHESTPLATE, 5f);
                        put(Item.CHAIN_LEGGINGS, 4f);
                        put(Item.CHAIN_BOOTS, 1f);
                        put(Item.GOLD_HELMET, 1f);
                        put(Item.GOLD_CHESTPLATE, 5f);
                        put(Item.GOLD_LEGGINGS, 3f);
                        put(Item.GOLD_BOOTS, 1f);
                        put(Item.IRON_HELMET, 2f);
                        put(Item.IRON_CHESTPLATE, 6f);
                        put(Item.IRON_LEGGINGS, 5f);
                        put(Item.IRON_BOOTS, 2f);
                        put(Item.DIAMOND_HELMET, 3f);
                        put(Item.DIAMOND_CHESTPLATE, 8f);
                        put(Item.DIAMOND_LEGGINGS, 6f);
                        put(Item.DIAMOND_BOOTS, 3f);
                    }
                };

                float points = 0;
                for (Item i : ((Player) player).getInventory().getArmorContents()) {
                    points += armorValues.getOrDefault(i.getId(), 0f);
                }

                damage.put(EntityDamageEvent.DamageModifier.ARMOR,
                        (float) (damage.getOrDefault(EntityDamageEvent.DamageModifier.ARMOR, 0f) - Math.floor(damage.getOrDefault(EntityDamageEvent.DamageModifier.BASE, 1f) * points * 0.04)));
            }

            player.attack(new EntityDamageByEntityEvent(this, player, EntityDamageEvent.DamageCause.ENTITY_ATTACK, damage));
        }
    }

    @Override
    public Item[] getDrops() {
        if (this.size == SIZE_BIG) {
            CreatureSpawnEvent ev = new CreatureSpawnEvent(NETWORK_ID, this, this.namedTag, CreatureSpawnEvent.SpawnReason.SLIME_SPLIT);
            level.getServer().getPluginManager().callEvent(ev);

            if (ev.isCancelled()) {
                return new Item[0];
            }

            MagmaCube entity = (MagmaCube) Entity.createEntity("MagmaCube", this);

            if (entity != null) {
                entity.size = SIZE_MEDIUM;
                entity.setScale(0.51f + entity.size * 0.51f);
                entity.spawnToAll();
            }

            return new Item[0];
        } else if (this.size == SIZE_MEDIUM) {
            CreatureSpawnEvent ev = new CreatureSpawnEvent(NETWORK_ID, this, this.namedTag, CreatureSpawnEvent.SpawnReason.SLIME_SPLIT);
            level.getServer().getPluginManager().callEvent(ev);

            if (ev.isCancelled()) {
                return new Item[0];
            }

            MagmaCube entity = (MagmaCube) Entity.createEntity("MagmaCube", this);

            if (entity != null) {
                entity.size = SIZE_SMALL;
                entity.setScale(0.51f + entity.size * 0.51f);
                entity.spawnToAll();
            }

            return new Item[0];
        } else {
            List<Item> drops = new ArrayList<>();

            if (this.lastDamageCause instanceof EntityDamageByEntityEvent && !this.isBaby()) {
                drops.add(Item.get(Item.MAGMA_CREAM, 0, Utils.rand(0, 1)));
            }

            return drops.toArray(new Item[0]);
        }
    }

    @Override
    public int getKillExperience() {
        if (this.size == SIZE_BIG) return 4;
        if (this.size == SIZE_MEDIUM) return 2;
        if (this.size == SIZE_SMALL) return 1;
        return 0;
    }

    @Override
    public String getName() {
        return "Magma Cube";
    }

    @Override
    public boolean entityBaseTick(int tickDiff) {
        if (getServer().getDifficulty() == 0) {
            this.close();
            return true;
        }

        return super.entityBaseTick(tickDiff);
    }
}
