package br.com.atocf.pedidoprocessor.repository;

import br.com.atocf.pedidoprocessor.model.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Long> {

}
