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
import utils.Categoria;
import utils.Unitario;

@ManagedBean
@SessionScoped
public class GerenciaEstoqueBean implements Serializable {
	private static final long serialVersionUID = 1L;

	private Elemento elemento;

	private Componente componente;

	private Produto produto;

	// Vari�vel List utilizado na consulta do estoque de componentes, e na aquisi��o de componente;
	private List<Componente> listaDeComponente;

	private Aquisicao aquisicao;

	// Vari�vel utilizado na lista de composi��o de componentes do produto;
	private List<Componente> listaDeComponenteDoProduto;

	// Objeto utilizado para adicional a lista de componentes do produto;
	private Componente componenteDoProduto;
	
	// Vari�vel List utilizado na composi��o de uma lista no qual ser� add na tabela associativa ComponenteDoProduto; 
	private List<ComponenteDoProduto> listaDaTabAssociativa;
	
	// Objeto utilizado para criar a chave primaria composta da tabela associativa CompondenteDoProduto;
	protected ComponenteProdutoPK pk;

	// Vari�vel utilizado na composi��o de componentes do produto;
	private float qtdUtilizado;

	// Vari�vel utilizado no cadastro de componente para salvar a quantidade m�nima de componentes desejado no estoque de componentes;
	private float estoqueMinimo;
	
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
		this.listaDeComponenteDoProduto = new ArrayList<Componente>();
		this.pk = new ComponenteProdutoPK();
	}

	@PostConstruct
	public void init() {
		this.listaDeComponente = daoComponente.listaComponente();
	}

	public void salvar() {
		context = FacesContext.getCurrentInstance();
		setDoTipo(elemento.getDescricao());
		setFoiCadastrado(elemento.getTipoElemento());
		if (elemento.getTipoElemento().equalsIgnoreCase("ingrediente") || elemento.getTipoElemento().equalsIgnoreCase("embalagem")) {
			componente = new Componente(elemento.getDescricao(), elemento.getTipoElemento(), elemento.getTipoUnitario(), getEstoqueMinimo());
			if (daoComponente.salvar(componente)) {
				elemento = new Elemento();
				componente = new Componente();
				setEstoqueMinimo(0);
				init();
				context.addMessage(null, new FacesMessage("Sucesso", "cadastrado " + getFoiCadastrado() + " " + getDoTipo()));
			} else {
				context.addMessage(null, new FacesMessage("Erro", "N�o foi possivel realizar o cadastro " + getFoiCadastrado() + " " + getDoTipo()));
			}
		} else {
			produto = new Produto(elemento.getDescricao(), elemento.getTipoElemento(), elemento.getValor());
			if (daoProduto.salvar(produto)) {
				produto = daoProduto.buscarPorCod(daoProduto.getLastInsertId());
				listaDaTabAssociativa = produto.getComponentes();
				for(Componente c: listaDeComponenteDoProduto) {
					pk.setCodProduto(produto.getCodigo());
					pk.setCodComponente(c.getCodigo());
					listaDaTabAssociativa.add(new ComponenteDoProduto(pk, c.getValor()));
					pk = new ComponenteProdutoPK();
				}
				produto.setComponentes(listaDaTabAssociativa);
				daoProduto.atualizar(produto);
				produto = new Produto();
				elemento = new Elemento();
				listaDeComponenteDoProduto = new ArrayList<Componente>();
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
		this.componenteDoProduto.setValor(getQtdUtilizado());
		setQtdUtilizado(0);
		this.componente = new Componente();
	}

	public void salvarAquisicao() {
		context = FacesContext.getCurrentInstance();
		aquisicao.setComponenteAdquirido(daoComponente.buscarPorCod(componente.getCodigo()));
		componente = aquisicao.getComponenteAdquirido();
		
		//Valor total em estoque de componentes � utilizado o valor unit�rio de custo m�dio;
		float qtdEstoqueAtual, qtdEntrada, precoMedioAtual, precoCompra;
		qtdEstoqueAtual = componente.getEstoqueAtual();
		qtdEntrada = aquisicao.getQtdAdquirida();
		precoMedioAtual = componente.getValor();
		precoCompra = aquisicao.getCustoDaAquisicao();
		float novoPrecoMedio = 0;
		if(componente.getEstoqueAtual() != 0) {
			novoPrecoMedio = (qtdEstoqueAtual * precoMedioAtual + qtdEntrada * (precoCompra / qtdEntrada)) / (qtdEstoqueAtual + qtdEntrada);
		}else {
			novoPrecoMedio = precoCompra / qtdEntrada;
		}
		componente.setValor(novoPrecoMedio);		
		componente.setEstoqueAtual(qtdEstoqueAtual + qtdEntrada);
		aquisicao.setDataDaAquisicao(new Date());
		if (daoComponente.salvarAquisicaoDeComponente(aquisicao) && daoComponente.atualizarEstoque(componente)) {
			componente = new Componente();
			aquisicao = new Aquisicao();
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

	public float getEstoqueMinimo() {
		return estoqueMinimo;
	}

	public void setEstoqueMinimo(float estoqueMinimo) {
		this.estoqueMinimo = estoqueMinimo;
	}

	public List<Componente> getListaDeComponente() {
		return listaDeComponente;
	}

	public void setListaDeComponente(List<Componente> listaDeComponente) {
		this.listaDeComponente = listaDeComponente;
	}

	public List<Componente> getListaDeComponenteDoProduto() {
		return listaDeComponenteDoProduto;
	}

	public void setListaDeComponenteDoProduto(List<Componente> listaDeComponenteDoProduto) {
		this.listaDeComponenteDoProduto = listaDeComponenteDoProduto;
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
	
	public List<ComponenteDoProduto> getListaDaTabAssociativa() {
		return listaDaTabAssociativa;
	}

	public void setListaDaTabAssociativa(List<ComponenteDoProduto> listaDaTabAssociativa) {
		this.listaDaTabAssociativa = listaDaTabAssociativa;
	}

	public float getQtdUtilizado() {
		return qtdUtilizado;
	}

	public void setQtdUtilizado(float qtdUtilizado) {
		this.qtdUtilizado = qtdUtilizado;
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
