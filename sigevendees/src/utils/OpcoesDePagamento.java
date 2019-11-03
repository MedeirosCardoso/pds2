package utils;

public enum OpcoesDePagamento {
	DINHEIRO("Dinheiro"),
	CREDITO("Cart�o Credito"),
	DEBITO("Cart�o Debito"),
	PRAZO("a Prazo");

	private final String descricao;

	OpcoesDePagamento(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}
}
