package br.com.atocf.pedido.repository;

import br.com.atocf.pedido.model.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Long> {

    List<Orders> findByOrderId(Long orderId);

    @Query("SELECT o FROM Orders o WHERE o.date >= :dataInicio AND o.date <= :dataFim")
    List<Orders> findByDateRange(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);

    @Query("SELECT o FROM Orders o WHERE o.orderId = :orderId AND o.date >= :dataInicio AND o.date <= :dataFim")
    List<Orders> findByOrderIdAndDateRange(@Param("orderId") Long orderId, @Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);
}
