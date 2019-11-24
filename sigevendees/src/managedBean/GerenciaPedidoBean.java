package managedBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import dao.ClienteDao;
import dao.ComponenteDao;
import dao.PedidoDao;
import dao.ProdutoDao;
import entity.Cliente;
import entity.ComponenteDoProduto;
import entity.ItemDoPedido;
import entity.ItemDoPedidoPK;
import entity.Pedido;
import entity.Produto;
import enumOpc.Situacao;

@ManagedBean
@SessionScoped
public class GerenciaPedidoBean implements Serializable {
	private static final long serialVersionUID = 1L;

	private Pedido pedido;
	protected String qtdProduto;
	
	private Pedido pedidoSelecionado;
	private List<Pedido> pedidosSelecionados;

	private List<Pedido> pedidosItemProzuzir;
	private List<Pedido> pedidosItemProzuzido;

	private Produto produto;

	private Cliente cliente;
	protected String numeroTelefone;

	protected List<Produto> produtos;

	private Produto iten;
	
	protected ItemDoPedidoPK pk;

	protected FacesContext context;

	@Inject
	private PedidoDao daoPedido = new PedidoDao();
	@Inject
	private ProdutoDao daoProduto = new ProdutoDao();
	@Inject
	private ClienteDao daoCliente = new ClienteDao();
	@Inject
	private ComponenteDao daoComponente = new ComponenteDao();

	public GerenciaPedidoBean() {
		this.pedido = new Pedido();
		this.produto = new Produto();
		this.cliente = new Cliente();
		this.produtos = new ArrayList<Produto>();
		this.iten = new Produto();
		this.pk = new ItemDoPedidoPK();
	}

	@PostConstruct
	public void init() {
		this.pedidosItemProzuzir = daoPedido.listarPedidosItemProduzir();
		this.pedidosItemProzuzido = daoPedido.listarPedidosItemProduzido();
		this.cliente.setNumTelefone(1);
	}

	public void salvar() {
		context = FacesContext.getCurrentInstance();
		this.pedido.setCliente(cliente);
		this.pedido.setDataSolicitado(new Date());
		this.pedido.setDataEntrega(pedido.getDataEntrega());
		if (daoPedido.salvar(pedido)) {
			int lastId = daoPedido.getLastInsertId();
			pedido = daoPedido.buscarPorCod(lastId);
			List<ItemDoPedido> itens = pedido.getItens();
			for (Produto p : produtos) {
				pk.setCodPedido(pedido.getCodPedido());
				pk.setCodProduto(p.getCodigo());
				itens.add(new ItemDoPedido(pk, (int) p.getValor(), 0, p.getValor() * produto.getValor()));
				pk = new ItemDoPedidoPK();
			}
			pedido.setItens(itens);
			daoPedido.atualizar(pedido);
			pedido = new Pedido();
			produto = new Produto();
			cliente = new Cliente();
			produtos = new ArrayList<Produto>();
			setNumeroTelefone(null);
			init();
			context.addMessage(null, new FacesMessage("Sucesso", "Pedido incluido"));
		} else {
			context.addMessage(null, new FacesMessage("Erro", "N�o foi possivel incluir o pedido!"));
		}
	}

	public void produzido() {
		for (Pedido p : getPedidosSelecionados()) {
			for (ItemDoPedido ip : p.getItens()) {
				ip.setStatusDoItem(Situacao.PRODUZIDO.toString());
				ip.setDataProduzido(new Date());
				for (ComponenteDoProduto c : ip.getProduto().getComponentes()) {
					float novoEstoque = c.getComponente().getEstoqueAtual() - (c.getQtdUtilizada()*ip.getQtdProduto());
					c.getComponente().setEstoqueAtual(novoEstoque);
					daoComponente.atualizar(c.getComponente());
				}
			}
			daoPedido.atualizar(p);
		}
		init();
	}

	public void excluir() {
		for (Pedido p : getPedidosSelecionados()) {
			daoPedido.deletarPedido(p.getCodPedido());
		}
		init();
	}

	public void buscarCliente() {
		context = FacesContext.getCurrentInstance();
		if (!getNumeroTelefone().isEmpty()) {
			cliente.setNumTelefone(Integer.parseInt(getNumeroTelefone()));
		}

		if (cliente.getNomeCliente().isEmpty() && cliente.getNumTelefone() > 0) {
			this.cliente = daoCliente.buscarPornumTelefone(cliente.getNumTelefone());
		} else if (cliente.getNumTelefone() <= 0 && !cliente.getNomeCliente().isEmpty()) {
			this.cliente = daoCliente.buscarPornome(cliente.getNomeCliente());
		}

		if (cliente != null) {
			setNumeroTelefone(Integer.toString(cliente.getNumTelefone()));
			this.cliente.setNumTelefone(cliente.getNumTelefone());
			this.cliente.setNomeCliente(cliente.getNomeCliente());
		} else {
			cliente = new Cliente();
			context.addMessage(null, new FacesMessage("Aviso", "Cliente n�o possui cadastro"));
		}
	}

	public void buscarProduto() {
		iten = daoProduto.buscarPorCod(produto.getCodigo());
		produto = daoProduto.buscarPorCod(produto.getCodigo());
	}

	public void reinitItenDoPedido() {
		iten.setValor(Float.parseFloat(getQtdProduto()));
		setQtdProduto(null);
	}

// A baixo est�o todos os m�todos GET e SET dos atributos da classe;
	public Pedido getPedido() {
		return pedido;
	}

	public void setPedido(Pedido pedido) {
		this.pedido = pedido;
	}

	public List<Pedido> getPedidosItemProzuzir() {
		return pedidosItemProzuzir;
	}

	public void setPedidosItemProzuzir(List<Pedido> pedidosItemProzuzir) {
		this.pedidosItemProzuzir = pedidosItemProzuzir;
	}

	public List<Pedido> getPedidosItemProzuzido() {
		return pedidosItemProzuzido;
	}

	public void setPedidosItemProzuzido(List<Pedido> pedidosItemProzuzido) {
		this.pedidosItemProzuzido = pedidosItemProzuzido;
	}

	public Produto getProduto() {
		return produto;
	}

	public void setProduto(Produto produto) {
		this.produto = produto;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public List<Produto> getProdutos() {
		return produtos;
	}

	public void setProdutos(List<Produto> produtos) {
		this.produtos = produtos;
	}

	public Produto getIten() {
		return iten;
	}

	public void setIten(Produto iten) {
		this.iten = iten;
	}

	public ItemDoPedidoPK getPk() {
		return pk;
	}

	public void setPk(ItemDoPedidoPK pk) {
		this.pk = pk;
	}

	public Pedido getPedidoSelecionado() {
		return pedidoSelecionado;
	}

	public void setPedidoSelecionado(Pedido pedidoSelecionado) {
		this.pedidoSelecionado = pedidoSelecionado;
	}

	public String getQtdProduto() {
		return qtdProduto;
	}

	public void setQtdProduto(String qtdProduto) {
		this.qtdProduto = qtdProduto;
	}

	public String getNumeroTelefone() {
		return numeroTelefone;
	}

	public void setNumeroTelefone(String numeroTelefone) {
		this.numeroTelefone = numeroTelefone;
	}

	public List<Pedido> getPedidosSelecionados() {
		return pedidosSelecionados;
	}

	public void setPedidosSelecionados(List<Pedido> pedidosSelecionados) {
		this.pedidosSelecionados = pedidosSelecionados;
	}
}
