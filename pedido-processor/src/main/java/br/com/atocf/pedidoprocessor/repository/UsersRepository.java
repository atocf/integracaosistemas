package br.com.atocf.pedidoprocessor.repository;

import br.com.atocf.pedidoprocessor.model.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {

    Users findByUserId(Long id);
}
