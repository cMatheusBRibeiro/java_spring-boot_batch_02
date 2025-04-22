package br.com.alura.bytebank.pagamentos;

import jakarta.annotation.Resource;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.MultiResourceItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;

@Configuration
public class PagamentosJobConfiguration {

    private final PlatformTransactionManager transactionManager;

    public PagamentosJobConfiguration(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Bean
    public Job job(JobRepository jobRepository, Step processarPagamentos) {
        return new JobBuilder("pagamentos", jobRepository)
                .start(processarPagamentos)
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    public Step processarPagamentos(
            JobRepository jobRepository,
            ItemReader<Pagamento> reader,
            ItemWriter<Pagamento> writer
    ) {
        return new StepBuilder("processar-pagamentos", jobRepository)
                .<Pagamento, Pagamento>chunk(10, transactionManager)
                .reader(reader)
                .writer(writer)
                .build();
    }

    @Bean
    public ItemReader<Pagamento> reader() throws IOException {
        return new MultiResourceItemReaderBuilder<Pagamento>()
                .name("processar-pagamentos-reader")
                .resources(new PathMatchingResourcePatternResolver().getResources("file:files/*.csv"))
                .delegate(pagamentoReader())
                .build();
    }

    @Bean
    public FlatFileItemReader<Pagamento> pagamentoReader() {
        return new FlatFileItemReaderBuilder<Pagamento>()
                .name("processar-pagamentos-file-reader")
                .resource(new FileSystemResource("dummy.csv"))
                .comments("Nome|")
                .delimited()
                .delimiter("|")
                .names("nome", "cpf", "agencia", "conta", "valor", "mesReferencia")
                .fieldSetMapper(new PagamentosMapper())
                .build();
    }

    @Bean
    public ItemWriter<Pagamento> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Pagamento>()
                .dataSource(dataSource)
                .sql("""
                        INSERT INTO pagamento (
                            nome,
                            cpf,
                            agencia,
                            conta,
                            valor,
                            mes_referencia,
                            data_hora_importacao
                        ) VALUES (
                            :nome,
                            :cpf,
                            :agencia,
                            :conta,
                            :valor,
                            :mesReferencia,
                            :dataHoraImportacao
                        )
                        """)
                .itemSqlParameterSourceProvider(
                        new BeanPropertyItemSqlParameterSourceProvider<Pagamento>()
                )
                .build();
    }

}
