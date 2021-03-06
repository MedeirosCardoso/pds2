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
import dao.ComponenteDao;
import dao.ProdutoDao;
import entity.Aquisicao;
import entity.Componente;
import entity.ComponenteDoProduto;
import entity.ComponenteProdutoPK;
import entity.Elemento;
import entity.Produto;
import enumOpc.Categoria;
import enumOpc.Unitario;

@ManagedBean
@SessionScoped
public class GerenciaEstoqueBean implements Serializable {
	private static final long serialVersionUID = 1L;

	private Elemento elemento;

	private Componente componente;

	private Produto produto;
	private List<Produto> listaDeProdutos;

	// Vari�vel List utilizado na consulta do estoque de componentes, e na aquisi��o de componente;
	private List<Componente> listaDeComponentes;

	private Aquisicao aquisicao;
	
	// Vari�veis utilizado nos inputs id="qtd", id="vlrCusto", para n�o iniciar os inputs com o valor 0 (default) do atributo da classe;
	protected String valorDaQtdAdquirida;
	protected String valorDoCustoDaAquisicao;
	
	// Vari�vel utilizado na lista de composi��o de componentes do produto;
	private List<Componente> listaDeComponentesDoProduto;

	// Objeto utilizado para adicional a lista de componentes do produto;
	private Componente componenteDoProduto;
	
	// Objeto utilizado para criar a chave primaria composta da tabela associativa CompondenteDoProduto;
	protected ComponenteProdutoPK pk;

	// Vari�vel utilizado na composi��o de componentes do produto;
	private String qtdUtilizado;

	// Vari�vel utilizado no cadastro de componente para salvar a quantidade m�nima de componentes desejado no estoque de componentes;
	private String estoqueMinimo;
	
	// Vari�vel utilizado no input id="valorVenda", para n�o iniciar o input com o valor 0 (default) do atributo da classe;
	private String valorVenda;
	
	// Vari�vel utilizado para apresentar na mensagem ao comerciante a descri��o do produto ou componente que foi cadastrado no BD;
	protected String foiCadastrado;
	
	// Vari�vel utilizado para apresentar na mensagem ao comerciante o tipo do produto(bolo ou salgado) ou componente(ingrediente ou embalagem) que foi cadastrado no BD;
	protected String doTipo;
	
	// Vari�vel utilizado para renderizar os inputs necess�rio para o cadastro de componente e ou produto;
	private boolean tipoDoCadastro;

	protected FacesContext context;

	@Inject
	private ComponenteDao daoComponente = new ComponenteDao();
	@Inject
	private ProdutoDao daoProduto = new ProdutoDao();

	public GerenciaEstoqueBean() {
		this.tipoDoCadastro = false;
		this.elemento = new Elemento();
		this.componente = new Componente();
		this.componenteDoProduto = new Componente();
		this.produto = new Produto();
		this.aquisicao = new Aquisicao();
		this.listaDeComponentesDoProduto = new ArrayList<Componente>();
		this.pk = new ComponenteProdutoPK();
	}

	@PostConstruct
	public void init() {
		this.listaDeComponentes = daoComponente.listarComponentes();
		this.listaDeProdutos = daoProduto.listarProdutos();
	}

	public void salvar() {
		context = FacesContext.getCurrentInstance();
		setDoTipo(elemento.getDescricao());
		setFoiCadastrado(elemento.getTipoElemento());
		if (elemento.getTipoElemento().equalsIgnoreCase("ingrediente") || elemento.getTipoElemento().equalsIgnoreCase("embalagem")) {
			componente = new Componente(elemento.getDescricao(), elemento.getTipoElemento(), elemento.getTipoUnitario(), Float.parseFloat(getEstoqueMinimo()));
			if (daoComponente.salvar(componente)) {
				elemento = new Elemento();
				componente = new Componente();
				setEstoqueMinimo(null);
				init();
				context.addMessage(null, new FacesMessage("Sucesso", "cadastrado " + getFoiCadastrado() + " " + getDoTipo()));
			} else {
				context.addMessage(null, new FacesMessage("Erro", "N�o foi possivel realizar o cadastro " + getFoiCadastrado() + " " + getDoTipo()));
			}
		} else {
			produto = new Produto(elemento.getDescricao(), elemento.getTipoElemento(), Float.parseFloat(getValorVenda()));
			if (daoProduto.salvar(produto)) {
				produto = daoProduto.buscarPorCod(daoProduto.getLastInsertId());
				List<ComponenteDoProduto> componentes = produto.getComponentes();
				for(Componente c: listaDeComponentesDoProduto) {
					pk.setCodProduto(produto.getCodigo());
					pk.setCodComponente(c.getCodigo());
					componentes.add(new ComponenteDoProduto(pk, c.getValor()));
					pk = new ComponenteProdutoPK();
				}
				produto.setComponentes(componentes);
				daoProduto.atualizar(produto);
				produto = new Produto();
				setValorVenda(null);
				elemento = new Elemento();
				listaDeComponentesDoProduto = new ArrayList<Componente>();
				init();
				context.addMessage(null, new FacesMessage("Sucesso", "cadastrado " + getFoiCadastrado() + " " + getDoTipo()));
			} else {
				context.addMessage(null, new FacesMessage("Erro", "N�o foi possivel realizar o cadastro " + getFoiCadastrado() + " " + getDoTipo()));
			}
		}
	}
	
	// Busca o componente selecionado no input selectOneMenu id="listaDeComponentes" e adiciona ao componenteDoProduto;
	public void buscarComponente() {
		this.componenteDoProduto = daoComponente.buscarPorCod(this.componente.getCodigo());
	}
	
	/*
	 * Atribui ao valor de custo do componente retornado do m�todo buscarComponente(),
	 * pois no componeteDoProduto o atributo valor � utilizado para informar a
	 * quantidade utilizado desse componente na composi��o do produto;
	 * Cria um novo componente a ser utilizado no produto;
	 */
	public void reinitComponenteDoProduto() {
		this.componenteDoProduto.setValor(Float.parseFloat(getQtdUtilizado()));
		setQtdUtilizado(null);
		this.componente = new Componente();
	}

	public void salvarAquisicao() {
		context = FacesContext.getCurrentInstance();
		aquisicao.setComponenteAdquirido(daoComponente.buscarPorCod(componente.getCodigo()));
		componente = aquisicao.getComponenteAdquirido();
		
		//Valor total em estoque de componentes � utilizado o valor unit�rio de custo m�dio;
		float qtdEstoqueAtual, qtdEntrada, precoMedioAtual, precoCompra;
		qtdEstoqueAtual = componente.getEstoqueAtual();
		qtdEntrada = Float.parseFloat(getValorDaQtdAdquirida());
		precoMedioAtual = componente.getValor();
		precoCompra = Float.parseFloat(getValorDoCustoDaAquisicao());
		float novoPrecoMedio = 0;
		if(componente.getEstoqueAtual() != 0) {
			novoPrecoMedio = (qtdEstoqueAtual * precoMedioAtual + qtdEntrada * (precoCompra / qtdEntrada)) / (qtdEstoqueAtual + qtdEntrada);
		}else {
			novoPrecoMedio = precoCompra / qtdEntrada;
		}
		componente.setValor(novoPrecoMedio);		
		componente.setEstoqueAtual(qtdEstoqueAtual + qtdEntrada);
		aquisicao.setCustoDaAquisicao(precoCompra);
		aquisicao.setQtdAdquirida(qtdEntrada);
		aquisicao.setDataDaAquisicao(new Date());
		if (daoComponente.salvarAquisicaoDeComponente(aquisicao) && daoComponente.atualizarEstoque(componente)) {
			componente = new Componente();
			aquisicao = new Aquisicao();
			setValorDaQtdAdquirida(null);
			setValorDoCustoDaAquisicao(null);
			init();
			context.addMessage(null, new FacesMessage("Sucesso", "Estoque do componente atualizado"));
		} else {
			context.addMessage(null, new FacesMessage("Erro", "N�o foi possivel realizar o registro da aquisi��o"));
		}
	}

	/*
	 * M�todo chamado no Ajax do selectOneRadio(tipoCadastro) ao selecionar o tipo
	 * de cadastro (bolo, salgado,ingrediente,embalagem) a ser realizado;
	 */
	public void inputsCadastro() {
		if (elemento.getTipoElemento().equalsIgnoreCase("bolo") || elemento.getTipoElemento().equalsIgnoreCase("salgado")) {
			setTipoCadastro(true);
		} else {
			setTipoCadastro(false);
		}
	}
	
	//Retorna todas as op��es de tipos unit�rios que podem ser utilizado no cadastro de Componente;
	public Unitario[] getOpcoes() {
		return Unitario.values();
	}
	
	//Retorna todas as op��es de categoria que podem ser utilizado no cadastro do elemento; 
	public Categoria[] getCategoriaElemento() {
		return Categoria.values();
	}
	
// A baixo est�o todos os m�todos GET e SET dos atributos da classe;
	
	public boolean isTipoCadastro() {
		return tipoDoCadastro;
	}

	public void setTipoCadastro(boolean tipoCadastro) {
		this.tipoDoCadastro = tipoCadastro;
	}

	public Elemento getElemento() {
		return elemento;
	}

	public void setElemento(Elemento elemento) {
		this.elemento = elemento;
	}

	public Componente getComponente() {
		return componente;
	}

	public void setComponente(Componente componente) {
		this.componente = componente;
	}

	public Aquisicao getAquisicao() {
		return aquisicao;
	}

	public void setAquisicao(Aquisicao aquisicao) {
		this.aquisicao = aquisicao;
	}

	public String getEstoqueMinimo() {
		return estoqueMinimo;
	}

	public void setEstoqueMinimo(String estoqueMinimo) {
		this.estoqueMinimo = estoqueMinimo;
	}

	public List<Componente> getListaDeComponentes() {
		return listaDeComponentes;
	}

	public void setListaDeComponentes(List<Componente> listaDeComponente) {
		this.listaDeComponentes = listaDeComponente;
	}

	public List<Componente> getListaDeComponentesDoProduto() {
		return listaDeComponentesDoProduto;
	}

	public void setListaDeComponentesDoProduto(List<Componente> listaDeComponenteDoProduto) {
		this.listaDeComponentesDoProduto = listaDeComponenteDoProduto;
	}

	public Componente getComponenteDoProduto() {
		return componenteDoProduto;
	}

	public void setComponenteDoProduto(Componente componenteDoProduto) {
		this.componenteDoProduto = componenteDoProduto;
	}

	public Produto getProduto() {
		return produto;
	}

	public void setProduto(Produto produto) {
		this.produto = produto;
	}
	
	public List<Produto> getListaDeProdutos() {
		return listaDeProdutos;
	}

	public void setListaDeProdutos(List<Produto> listaDeProdutos) {
		this.listaDeProdutos = listaDeProdutos;
	}
	
	public String getQtdUtilizado() {
		return qtdUtilizado;
	}

	public void setQtdUtilizado(String qtdUtilizado) {
		this.qtdUtilizado = qtdUtilizado;
	}

	public String getValorVenda() {
		return valorVenda;
	}

	public void setValorVenda(String valorVenda) {
		this.valorVenda = valorVenda;
	}

	public String getValorDaQtdAdquirida() {
		return valorDaQtdAdquirida;
	}

	public void setValorDaQtdAdquirida(String valorDaQtdAdquirida) {
		this.valorDaQtdAdquirida = valorDaQtdAdquirida;
	}

	public String getValorDoCustoDaAquisicao() {
		return valorDoCustoDaAquisicao;
	}

	public void setValorDoCustoDaAquisicao(String valorDoCustoDaAquisicao) {
		this.valorDoCustoDaAquisicao = valorDoCustoDaAquisicao;
	}

	public String getFoiCadastrado() {
		return foiCadastrado;
	}

	public void setFoiCadastrado(String foiCadastrado) {
		this.foiCadastrado = foiCadastrado;
	}

	public String getDoTipo() {
		return doTipo;
	}

	public void setDoTipo(String mensagem) {
		this.doTipo = mensagem;
	}
}
