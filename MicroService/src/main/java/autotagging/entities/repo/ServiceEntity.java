package de.funkedigital.autotagging.entities.repo;

import de.funkedigital.autotagging.enums.ServiceEnum;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "services")
public class ServiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "service")
    private ServiceEnum serviceEnum;

    @Column(name = "sequence")
    private int sequence;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ServiceEnum getServiceEnum() {
        return serviceEnum;
    }

    public void setServiceEnum(ServiceEnum serviceEnum) {
        this.serviceEnum = serviceEnum;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof ServiceEntity)) return false;

        ServiceEntity that = (ServiceEntity) o;

        return new EqualsBuilder()
                .append(sequence, that.sequence)
                .append(serviceEnum, that.serviceEnum)
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(serviceEnum)
                .append(sequence)
                .toHashCode();
    }
}
