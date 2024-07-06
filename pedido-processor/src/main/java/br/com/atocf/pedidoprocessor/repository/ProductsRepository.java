package br.com.atocf.pedidoprocessor.repository;

import br.com.atocf.pedidoprocessor.model.entity.ProductId;
import br.com.atocf.pedidoprocessor.model.entity.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductsRepository extends JpaRepository<Products, ProductId> {

}
