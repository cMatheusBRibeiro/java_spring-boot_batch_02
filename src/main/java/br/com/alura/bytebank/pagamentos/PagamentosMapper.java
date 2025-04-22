package br.com.alura.bytebank.pagamentos;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import java.time.LocalDateTime;

public class PagamentosMapper implements FieldSetMapper<Pagamento> {

    @Override
    public Pagamento mapFieldSet(FieldSet fieldSet) throws BindException {
        var pagamento = new Pagamento();

        pagamento.setAgencia(fieldSet.readString("agencia"));
        pagamento.setConta(fieldSet.readString("conta"));
        pagamento.setCpf(fieldSet.readString("cpf"));
        pagamento.setMesReferencia(fieldSet.readString("mesReferencia"));
        pagamento.setNome(fieldSet.readString("nome"));
        pagamento.setValor(Double.parseDouble(fieldSet.readString("valor")));
        pagamento.setDataHoraImportacao(LocalDateTime.now());

        return pagamento;
    }

}
