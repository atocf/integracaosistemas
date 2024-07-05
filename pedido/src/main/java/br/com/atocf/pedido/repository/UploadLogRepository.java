package br.com.atocf.pedido.repository;

import br.com.atocf.pedido.model.entity.UploadLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UploadLogRepository extends JpaRepository<UploadLog, Long> {

}
