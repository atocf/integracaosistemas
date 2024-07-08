package br.com.atocf.pedido.model.dto;

import br.com.atocf.pedido.model.entity.Products;
import br.com.atocf.pedido.model.entity.UploadLog;
import br.com.atocf.pedido.model.entity.Users;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PedidoDto {

    private Users user;
    private UploadLog uploadLog;
    private Long orderId;
    private LocalDate date;
    private Double total;
    private List<Products> products;
}
