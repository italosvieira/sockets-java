public class Questao {
    private Integer numeroQuestao;
    private Integer numeroAlternativas;
    private Integer numeroAcertos;
    private Integer numeroErros;
    private Integer numeroAcertosTotais;
    private Integer numeroErrosTotais;

    public void incrementarAcertos(Integer numeroAcertos) {
        if (this.numeroAcertosTotais != null) {
            this.numeroAcertosTotais = this.numeroAcertosTotais + numeroAcertos;
        } else {
            this.numeroAcertosTotais = numeroAcertos;
        }
    }

    public void incrementarErros(Integer numeroErros) {
        if (this.numeroErrosTotais != null) {
            this.numeroErrosTotais = this.numeroErrosTotais + numeroErros;
        } else {
            this.numeroErrosTotais = numeroErros;
        }
    }

    public Integer getNumeroAcertosTotais() {
        return numeroAcertosTotais;
    }

    public Integer getNumeroErrosTotais() {
        return numeroErrosTotais;
    }

    public Integer getNumeroQuestao() {
        return numeroQuestao;
    }

    public void setNumeroQuestao(Integer numeroQuestao) {
        this.numeroQuestao = numeroQuestao;
    }


    public void setNumeroAlternativas(Integer numeroAlternativas) {
        this.numeroAlternativas = numeroAlternativas;
    }

    public Integer getNumeroAcertos() {
        return numeroAcertos;
    }


    public Integer getNumeroErros() {
        return numeroErros;
    }

    public void setNumeroAcertos(Integer numeroAcertos) {
        this.numeroAcertos = numeroAcertos;
    }

    public void setNumeroErros(Integer numeroErros) {
        this.numeroErros = numeroErros;
    }

    @Override
    public int hashCode() {
        return this.getNumeroQuestao().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Questao && ((Questao) other).getNumeroQuestao().equals(this.getNumeroQuestao());

    }
}