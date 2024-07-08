package br.com.atocf.pedido.repository;

import br.com.atocf.pedido.model.entity.Orders;
import br.com.atocf.pedido.model.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Long> {

    List<Orders> findByOrderId(Long orderId);
}
