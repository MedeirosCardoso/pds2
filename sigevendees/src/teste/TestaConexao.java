package teste;

import javax.persistence.EntityManager;

import factoryConnection.FactoryJPA;

public class TestaConexao {

	public static void main(String[] args) {
		EntityManager conexao = FactoryJPA.getEntityManagerFactory().createEntityManager();

		if (conexao != null) {
			System.out.println("CONEX�O REALIZADA COM SUCESSO!");
		} else {
			System.out.println("N�O FOI POSSIVEL CONECTAR AO BANCO DE DADOS!");
		}
	}
}