package io.github.alpharlee.dogfight;

import io.github.alpharlee.dogfight.floatingentity.FloatingItem;
import io.github.alpharlee.dogfight.floatingentity.Target;
import io.github.alpharlee.dogfight.projectile.DfProjectile;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.List;

public enum MetadataTag {
    PROJECTILE("projectile"),
    PROJECTILE_PASSENGER("projectilePassenger"),
    //HOOP("hoop"), //TODO: Find valid reason to uncomment this
    FLOATING_ITEM("floatingItem"),
    TARGET("target");

    private String tagName;
    private static final String prefix = "dogfight.";

    private MetadataTag(String tagName) {
        this.tagName = tagName;
    }

    /**
     * Set the metadata value for this entity to the specified value, as associated with this tag.
     * The metadata name in whole is "dogfight.<tag name>" to reduce the odds of having multiple metadatas assigned to the same name
     *
     * @param entity
     * @param value
     * @author R Lee
     */
    public void setMetadata(Entity entity, Object value) {
        entity.setMetadata(prefix + this.tagName, new FixedMetadataValue(Dogfight.instance, value));
    }

    /**
     * Get the value associated to the metadata value assigned to this tag.
     * If {@link MetadataTag#setMetadata(Entity, Object)} has not been invoked for this entity prior to invoking this method, returns null
     * If more than one value is found for the same metadata tag, return the first one
     *
     * @param entity
     * @return
     * @author R Lee
     */
    public Object getMetadata(Entity entity) {
        List<MetadataValue> metaList = entity.getMetadata(prefix + this.tagName);

        if (!metaList.isEmpty()) {
            //Return first element found
            return metaList.get(0).value();
        } else {
            return null;
        }
    }

    /**
     * Return whether or not this entity has the assigned metadata tag on it
     *
     * @param entity
     * @return
     * @author R Lee
     */
    public boolean hasMetadata(Entity entity) {
        return entity.hasMetadata(prefix + this.tagName);
    }

    /**
     * Remove the metadata assigned to this entity with this tag. Removes metadata only from this plugin
     *
     * @param entity
     * @author R Lee
     */
    public void removeMetadata(Entity entity) {
        entity.removeMetadata(prefix + this.tagName, Dogfight.instance);
    }

    public String getFullName() {
        return prefix + this.tagName;
    }

    /**
     * Attempts to get the projectile associated to this entity, by checking tags PROJECTILE and PROJECTILE_PASSENGER in that order
     *
     * @param entity
     * @return DfProjectile from the first tag to find a projectile associated to the entity, or null if none found
     * @throws ClassCastException
     * @author R Lee
     */
    public static DfProjectile getProjectile(Entity entity) throws ClassCastException {
        MetadataTag[] tags = {MetadataTag.PROJECTILE, MetadataTag.PROJECTILE_PASSENGER};
        DfProjectile projectile;

        for (MetadataTag tag : tags) {
            projectile = getProjectile(entity, tag);
            if (projectile != null) {
                return projectile;
            }
        }

        return null;
    }

    /**
     * Attempts to cast entity with specified tag to DfProjectile
     *
     * @param entity
     * @param tag
     * @return
     * @author R Lee
     */
    private static DfProjectile getProjectile(Entity entity, MetadataTag tag) {
        Object value = tag.getMetadata(entity);

        if (value != null) {
            return (DfProjectile) value;
        } else {
            return null;
        }
    }

    /**
     * Convenience method to cast {@link MetadataTag#getMetadata(Entity)} into a {@link FloatingItem}, using FLOATING_ITEM tag
     *
     * @param item Item entity that is encapsulated within this floating item
     * @return FloatingItem or null if metadata not set for this entity
     * @throws ClassCastException if {@link MetadataTag#getMetadata(Entity)} cannot be cast to a FloatingItem
     * @author R Lee
     */
    public FloatingItem getFloatingItem(Item item) throws ClassCastException {
        Object value = MetadataTag.FLOATING_ITEM.getMetadata(item);

        if (value != null) {
            return (FloatingItem) value;
        } else {
            return null;
        }
    }

    /**
     * Convenience method to cast {@link MetadataTag#getMetadata(Entity)} into a {@link FloatingItem}, using FLOATING_ITEM tag
     *
     * @param target Target entity encapsulated within the target
     * @return Target or null if metadata not set for this entity
     * @throws ClassCastException if {@link MetadataTag#getMetadata(Entity)} cannot be cast to a target
     * @author R Lee
     */
    public Target getTarget(Entity target) throws ClassCastException {
        Object value = MetadataTag.TARGET.getMetadata(target);

        if (value != null) {
            return (Target) value;
        } else {
            return null;
        }
    }
}
