package io.github.alpharlee.dogfight.projectile;

import io.github.alpharlee.dogfight.MetadataTag;
import io.github.alpharlee.dogfight.registry.ProjectileRegistry;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SquidShot extends DfEntityProjectile {

    private static final PotionEffect blindnessEffect = new PotionEffect(PotionEffectType.BLINDNESS, 100, 2);
    public static final double speedMultiplier = 1.2;
    private static final int maxTicksLived = 500; //500 ticks = 25 seconds
    private static final double hitRadius = 3.75;
    private static final double damageValue = 0;
    private static final Particle hitParticle = Particle.SMOKE_LARGE;
    private static final Sound playerHitSound = Sound.BLOCK_SLIME_BLOCK_STEP;

    /**
     * Create a Dogfight Projectile
     *
     * @param projectileRegistry Projectile registry to store the projectile in
     * @param owner              Owner of projectile
     * @param projectileEntity   Projectile entity that represents this projectile
     */
    public SquidShot(ProjectileRegistry projectileRegistry, Player owner, Projectile projectileEntity) {
        super(projectileRegistry, owner, projectileEntity, projectileEntity.getVelocity().multiply(speedMultiplier),
                maxTicksLived, hitRadius, damageValue, hitParticle, playerHitSound);

        //Play squid hurt sound to player
        //1st parameter - Location: Location to play sound from
        //2nd parameter - Sound
        //3rd parameter - "volume": At 1, full volume but barely audible 15 blocks away
        //4th parameter - pitch: 1.0 for normal
        getOwner().playSound(getOwner().getLocation(), Sound.ENTITY_SQUID_HURT, 1, 1.3f);
    }

    @Override
    public ProjectileType getType() {
        return ProjectileType.SQUID_SHOT;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.SQUID;
    }

    @Override
    protected void onHit(Entity target) {
        super.onHit(target);

        if (target instanceof LivingEntity) {

            LivingEntity livingTarget = (LivingEntity) target;
            livingTarget.addPotionEffect(blindnessEffect);

            if (livingTarget instanceof Player) {
                Player player = (Player) livingTarget;
                player.resetTitle();
                player.sendTitle(ChatColor.RED + "You have been blinded!", "It will last for 5 seconds", 10, 70, 20);
            }
        }
    }

    @Override
    protected boolean validateNearbyEntity(Entity entity) {
        DfProjectile projectile = MetadataTag.getProjectile(entity);

        //Negate any projectiles launched by this player
        if (projectile != null && projectile.getOwner() == this.getOwner()) {
            return false; //Friendly projectile, not valid target
        } else {
            return super.validateNearbyEntity(entity); //Either projectile from different owner or no projectile found
        }
    }
}
