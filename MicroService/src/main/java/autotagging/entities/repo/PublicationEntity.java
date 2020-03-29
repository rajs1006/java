package de.funkedigital.autotagging.entities.repo;

import org.hibernate.annotations.ColumnTransformer;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "publications")
public class PublicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "publication")
    @ColumnTransformer(read = "LOWER(publication)")
    private String publication;

    @OneToMany(
            mappedBy = "publication",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    private Set<ScheduleEntity> scheduleEntities;

    @OneToOne
    @JoinColumn(name = "id")
    private PropertyEntity propertyEntity;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPublication() {
        return publication;
    }

    public void setPublication(String publication) {
        this.publication = publication;
    }

    public Set<ScheduleEntity> getScheduleEntities() {
        return scheduleEntities;
    }

    public void setScheduleEntities(Set<ScheduleEntity> scheduleEntities) {
        this.scheduleEntities = scheduleEntities;
    }

    public PropertyEntity getPropertyEntity() {
        return propertyEntity;
    }

    public void setPropertyEntity(PropertyEntity propertyEntity) {
        this.propertyEntity = propertyEntity;
    }

    /**
     * @return Sorted Array list
     */
    public List<ScheduleEntity> getSortedScheduleEntities() {
        List<ScheduleEntity> scheduleEntities = new ArrayList<>(this.scheduleEntities);
        // Sort based on load sequence
        Collections.sort(scheduleEntities);
        return scheduleEntities;
    }
}
