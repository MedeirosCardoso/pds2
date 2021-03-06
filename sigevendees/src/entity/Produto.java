package entity;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import enumOpc.Unitario;

@Entity
public class Produto extends Elemento {

	// C�digo do produto, ser� gerado pelo BD;
	@Id
	@GeneratedValue
	@Column(name = "codProduto")
	@Override
	public int getCodigo() {
		return super.getCodigo();
	}

	@Column(name = "desProduto", nullable = false)
	@Override
	public String getDescricao() {
		return super.getDescricao();
	}

	@Column(name = "tipoProduto", nullable = false)
	@Override
	public String getTipoElemento() {
		return super.getTipoElemento();
	}

	@Column(name = "tipoUnitario", nullable = false)
	@Override
	public String getTipoUnitario() {
		return super.getTipoUnitario();
	}

	@Column(name = "valorDeVenda", nullable = false)
	@Override
	public float getValor() {
		return super.getValor();
	}

	/* Lista que contem os componentes que fazem composi��o do produto */
	private List<ComponenteDoProduto> componentes;

	public Produto() {

	}

	public Produto(String descricao, String tipoElemento, float valor) {
		setDescricao(descricao);
		setTipoElemento(tipoElemento);
		setValor(valor);
		setTipoUnitario(Unitario.UND.toString());
	}

	@OneToMany(targetEntity = ComponenteDoProduto.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	public List<ComponenteDoProduto> getComponentes() {
		return componentes;
	}

	public void setComponentes(List<ComponenteDoProduto> componentes) {
		this.componentes = componentes;
	}

	@Override
	public String toString() {
		return "Produto [getCodigo()=" + getCodigo() + ", getDescricao()=" + getDescricao() + ", getTipoElemento()="
				+ getTipoElemento() + ", getTipoUnitario()=" + getTipoUnitario() + ", getValor()=" + getValor() + "]";
	}
}
