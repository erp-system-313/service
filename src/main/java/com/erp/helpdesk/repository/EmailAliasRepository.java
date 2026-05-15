package com.erp.helpdesk.repository;

import com.erp.helpdesk.entity.EmailAlias;
import com.erp.helpdesk.entity.EmailAlias.AliasModelType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailAliasRepository extends JpaRepository<EmailAlias, Long> {

    Optional<EmailAlias> findByFullAlias(String fullAlias);

    Optional<EmailAlias> findByAliasLocalPartAndAliasDomain(String localPart, String domain);

    List<EmailAlias> findByModelType(AliasModelType modelType);

    List<EmailAlias> findByActiveTrue();

    List<EmailAlias> findByTeamId(Long teamId);
}
