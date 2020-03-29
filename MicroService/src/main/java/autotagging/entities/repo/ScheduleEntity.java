package de.funkedigital.autotagging.entities.repo;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "schedules")
public class ScheduleEntity implements Comparable<ScheduleEntity> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "publication_id")
    private PublicationEntity publication;

    @OneToOne
    @JoinColumn(name = "service_id")
    private ServiceEntity service;

    @Column(name = "enabled")
    private Boolean enabled;

    @Column(name = "delay")
    private int delay;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PublicationEntity getPublication() {
        return publication;
    }

    public void setPublication(PublicationEntity publication) {
        this.publication = publication;
    }

    public ServiceEntity getService() {
        return service;
    }

    public void setService(ServiceEntity service) {
        this.service = service;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScheduleEntity)) return false;
        ScheduleEntity that = (ScheduleEntity) o;
        return delay == that.delay &&
                Objects.equals(id, that.id) &&
                Objects.equals(publication, that.publication) &&
                Objects.equals(service, that.service) &&
                Objects.equals(enabled, that.enabled);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(publication)
                .append(service)
                .append(enabled)
                .append(delay)
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(ScheduleEntity o) {
        if (o.getService().getSequence() > this.getService().getSequence()) return -1;
        else if (o.getService().getSequence() < this.getService().getSequence()) return 1;

        return 0;
    }
}
