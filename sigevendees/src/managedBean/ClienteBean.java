package managedBean;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import dao.ClienteDao;
import entity.Cliente;

@ManagedBean
@SessionScoped
public class ClienteBean implements Serializable {
	private static final long serialVersionUID = 1L;

	private Cliente cliente;

	// Vari�vel que recebera o cliente retornado da busca no BD;
	protected Cliente clienteRetornado;
	// Vari�vel utilizado no input id="telefoneCliente", para n�o iniciar o input com o valor 0 do atributo numTelefone da classe Cliente;
	protected String numeroTelerone;
	
	FacesContext context;

	@Inject
	private ClienteDao dao;

	@PostConstruct
	public void init() {
		this.cliente = new Cliente();
		this.dao = new ClienteDao();
	}

	public void salvar() {
		this.context = FacesContext.getCurrentInstance();
		this.cliente.setNumTelefone(Integer.parseInt(getNumeroTelerone()));
		if (dao.salvar(this.cliente)) {
			this.cliente = new Cliente();
			setNumeroTelerone(null);
			/*
			 * verifica se o cliente j� possui cadastro, se n�o possui cadastro e um novo
			 * cliente a ser cadastrado, se possui cadastro e um cliente a fazer atualiza��o
			 * de cadastro;
			 */
			if (clienteRetornado == null) {
				context.addMessage(null, new FacesMessage("Sucesso", "Cliente cadastrado"));
			} else {
				context.addMessage(null, new FacesMessage("Sucesso", "Cadastro atualizado"));
			}

		} else {
			context.addMessage(null, new FacesMessage("Erro", "N�o foi possivel realizar o cadastro"));
		}
	}

	/*
	 * quando o input id="telefoneCliente perde o foco faz a busca no BD do n�mero
	 * de telefone digitado no input;
	 */
	public void buscarCliente() {
		this.context = FacesContext.getCurrentInstance();
		
		int telefoneSolicitado = Integer.parseInt(getNumeroTelerone());
		if(telefoneSolicitado !=0) {
			clienteRetornado = dao.buscarPornumTelefone(telefoneSolicitado);
			if (clienteRetornado != null) {
				this.cliente.setNumTelefone(this.cliente.getNumTelefone());
				this.cliente.setNomeCliente(clienteRetornado.getNomeCliente());
				this.cliente.setEstabelecimentoCliente(clienteRetornado.getEstabelecimentoCliente());
				this.cliente.setObservacaoCliente(clienteRetornado.getObservacaoCliente());
			} else {
				this.cliente.setNumTelefone(Integer.parseInt(getNumeroTelerone()));
				this.cliente.setNomeCliente(null);
				this.cliente.setEstabelecimentoCliente(null);
				this.cliente.setObservacaoCliente(null);
				context.addMessage(null, new FacesMessage("Aviso", "Cliente n�o possui cadastro"));
			}
		}
	}

	public void excluirCliente() {
		this.context = FacesContext.getCurrentInstance();
		if (dao.deletarCliente(Integer.parseInt(getNumeroTelerone()))) {
			this.cliente = new Cliente();
			setNumeroTelerone(null);
			context.addMessage(null, new FacesMessage("Sucesso", "Cadastro do cliente excluido"));
		} else {

			context.addMessage(null, new FacesMessage("Erro", "N�o foi possivel excluir o cadastro do cliente"));
		}
	}

	/*
	 * M�todo utilizado no commandButton id="cancelarAcaoCliente", se o comerciante
	 * desistir de realizar o cadastro do cliente e j� ter preenchido os inputs,
	 * assim quando clicar no bot�o CANCELAR limpas os inputs da tela;
	 */
	public void resetInputs() {
		setNumeroTelerone(null);
		this.cliente.setNomeCliente(null);
		this.cliente.setEstabelecimentoCliente(null);
		this.cliente.setObservacaoCliente(null);
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public String getNumeroTelerone() {
		return numeroTelerone;
	}

	public void setNumeroTelerone(String numeroTelerone) {
		this.numeroTelerone = numeroTelerone;
	}
}
