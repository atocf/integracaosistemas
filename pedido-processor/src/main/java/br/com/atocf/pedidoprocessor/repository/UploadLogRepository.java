package br.com.atocf.pedidoprocessor.repository;

import br.com.atocf.pedidoprocessor.model.entity.UploadLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UploadLogRepository extends JpaRepository<UploadLog, Long> {

}
