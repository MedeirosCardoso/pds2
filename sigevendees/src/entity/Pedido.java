package entity;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class Pedido {
	
	@Id
	@GeneratedValue
	private int codPedido;
	
	@Column(nullable = true)
	private Cliente cliente;
	
	private List<ItemDoPedido> itens;
	
	@Column(nullable = false)
	private Date dataSolicitado;
	
	@Column(nullable = true)
	private Date dataEntrega;
	
	public Pedido() {

	}

	public Pedido(Cliente cliente, List<ItemDoPedido> itens, Date dataSolicitado, Date dataEntrega) {
		this.cliente = cliente;
		this.itens = itens;
		this.dataSolicitado = dataSolicitado;
		this.dataEntrega = dataEntrega;
	}

	public int getCodPedido() {
		return codPedido;
	}

	public void setCodPedido(int codPedido) {
		this.codPedido = codPedido;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}
	
	@OneToMany(targetEntity = ItemDoPedido.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	public List<ItemDoPedido> getItens() {
		return itens;
	}

	public void setItens(List<ItemDoPedido> itens) {
		this.itens = itens;
	}

	public Date getDataSolicitado() {
		return dataSolicitado;
	}

	public void setDataSolicitado(Date dataSolicitado) {
		this.dataSolicitado = dataSolicitado;
	}

	public Date getDataEntrega() {
		return dataEntrega;
	}

	public void setDataEntrega(Date dataEntrega) {
		this.dataEntrega = dataEntrega;
	}

	@Override
	public String toString() {
		return "Pedido [codPedido=" + codPedido + ", cliente=" + cliente + ", itens=" + itens + ", dataSolicitado="
				+ dataSolicitado + ", dataEntrega=" + dataEntrega + "]";
	}
}
