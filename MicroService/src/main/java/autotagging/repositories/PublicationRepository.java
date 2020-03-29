package de.funkedigital.autotagging.repositories;

import de.funkedigital.autotagging.entities.repo.PublicationEntity;
import de.funkedigital.autotagging.entities.repo.ScheduleEntity;
import de.funkedigital.autotagging.enums.ServiceEnum;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


/**
 * This repository is to interact with {@link PublicationEntity}, {@link ScheduleEntity}
 */
@Repository
public interface PublicationRepository extends CrudRepository<PublicationEntity, Long> {


    /**
     * This method enables/disables the state of service for particular publication
     *
     * @param state       True/False, enabled or disabled
     * @param publication For which publication te service need to be enabled/disabled.
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE ScheduleEntity sch set sch.enabled = :state , sch.delay = :interval where " +
            "(sch.publication = (select id from PublicationEntity atg where atg.publication = :publication)) and" +
            "(sch.service = (select id from ServiceEntity srv where srv.serviceEnum = :service))")
    void updateScheduleStatus(@Param("publication") String publication,
                              @Param("service") ServiceEnum service,
                              @Param("interval") int interval,
                              @Param("state") Boolean state);

    @Query("select sch.delay from ScheduleEntity as sch" +
            " where sch.publication = (select pub.id from PublicationEntity AS pub where pub.publication = :publication)" +
            " and sch.service = (select ser.id from ServiceEntity AS ser where ser.serviceEnum = :service)")
    int findDelayByPublicationAndService(@Param("publication") String publication,
                                         @Param("service") ServiceEnum service);


    @Query("select sch.enabled from ScheduleEntity as sch" +
            " where sch.publication = (select pub.id from PublicationEntity AS pub where pub.publication = :publication)" +
            " and sch.service = (select ser.id from ServiceEntity AS ser where ser.serviceEnum = :service)")
    boolean findEnabledByPublicationAndService(@Param("publication") String publication,
                                         @Param("service") ServiceEnum service);


}
