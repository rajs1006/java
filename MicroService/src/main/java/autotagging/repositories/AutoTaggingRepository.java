package de.funkedigital.autotagging.repositories;

import de.funkedigital.autotagging.entities.AutoTagging;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository
public interface AutoTaggingRepository extends CrudRepository<AutoTagging, Long> {

    @Query("SELECT sch.enabled FROM Schedule sch where (sch.publication.publication = :publication)")
    Boolean getSchedulingStatus(@Param("publication") String publication);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Schedule sch set sch.enabled = :state where " +
            "(sch.publication = (select id from AutoTagging atg where atg.publication = :publication))")
    void updateScheduleStatus(@Param("state") Boolean state,
                              @Param("publication") String publication);

}
