package br.com.atocf.pedido.repository;

import br.com.atocf.pedido.model.entity.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductsRepository extends JpaRepository<Products, Long> {

    List<Products> findByOrderId(Long orderId);
}
