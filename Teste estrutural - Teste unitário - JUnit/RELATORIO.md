# Relatório do grupo

Integrantes: André Tavares (24066498-2)

## Grafos e complexidade

Pra montar os grafos a gente seguiu essa lógica: cada lado de um `&&`/`||`
vira um nó de decisão separado (o segundo lado só entra se o primeiro não
resolver sozinho), cada `case` do switch é tratado como um `else if`, e todo
`return`/`throw` termina no mesmo nó final, senão não dá pra aplicar direito
o `V(G) = E - N + 2`. Laço conta como uma decisão só, não importa quantas
vezes ele roda.

Chamadas do `PedidoService.fechar`:

```mermaid
flowchart TD
    fechar["PedidoService.fechar"] --> descontos["PoliticaDesconto.calcular"]
    fechar --> fretes["CalculadoraFrete.calcular"]
    fechar --> risco["AnaliseRisco.avaliar"]
    fechar --> pagamentos["PagamentoService.pagar"]
    pagamentos --> processador["ProcessadorPagamento.autorizar (stub do teste)"]
```

`PoliticaDesconto.calcular`:

```mermaid
flowchart TD
    S1((start)) --> D1{subtotal < 0?}
    D1 -- true --> T1[throw IAE]
    D1 -- false --> D2{cliente.vip?}
    D2 -- true --> B1[desconto = 10%]
    D2 -- false --> D3{subtotal >= 50000?}
    D3 -- true --> B2[desconto = 5%]
    D3 -- false --> B3[desconto = 0]
    B1 --> D4a{cupom == null?}
    B2 --> D4a
    B3 --> D4a
    D4a -- true --> R1[return desconto]
    D4a -- false --> D4b{cupom.isBlank?}
    D4b -- true --> R1
    D4b -- false --> D5a{cupom == BEMVINDO?}
    D5a -- true --> D6a{comprasAnteriores == 0?}
    D5a -- false --> D5b{cupom == EXTRA10?}
    D6a -- true --> D6b{subtotal >= 10000?}
    D6a -- false --> J1[teto = subtotal*20%]
    D6b -- true --> B4[desconto += 2000]
    D6b -- false --> J1
    B4 --> J1
    D5b -- true --> D7{subtotal >= 20000?}
    D5b -- false --> T2[throw cupom desconhecido]
    D7 -- true --> B5[desconto += 10%]
    D7 -- false --> J1
    B5 --> J1
    J1 --> D8{desconto > teto?}
    D8 -- true --> R2[return teto]
    D8 -- false --> R3[return desconto]
    T1 --> END((end))
    R1 --> END
    T2 --> END
    R2 --> END
    R3 --> END
```

`CalculadoraFrete.calcular`:

```mermaid
flowchart TD
    S2((start)) --> E1{liquido < 0?}
    E1 -- true --> T3[throw IAE]
    E1 -- false --> E2{uf == PR?}
    E2 -- true --> F1[frete = 1200]
    E2 -- false --> E3{uf == SP?}
    E3 -- true --> F2[frete = 2000]
    E3 -- false --> E4{uf == RJ?}
    E4 -- true --> F2
    E4 -- false --> F3[frete = 3000]
    F1 --> E5{excedente > 0?}
    F2 --> E5
    F3 --> E5
    E5 -- true --> F4[frete += 300; excedente -= 1000]
    F4 --> E5
    E5 -- false --> E6a{liquido >= 30000?}
    E6a -- true --> E6b{!expresso?}
    E6a -- false --> E7{vip?}
    E6b -- true --> F5[frete = 0]
    E6b -- false --> E7
    F5 --> E7
    E7 -- true --> F6[frete /= 2]
    E7 -- false --> E8{expresso?}
    F6 --> E8
    E8 -- true --> F7[frete += 1500]
    E8 -- false --> E9{temFragil?}
    F7 --> E9
    E9 -- true --> F8[frete += 500]
    E9 -- false --> R4[return frete]
    F8 --> R4
    T3 --> END2((end))
    R4 --> END2
```

`AnaliseRisco.avaliar`:

```mermaid
flowchart TD
    S3((start)) --> G1{total < 0?}
    G1 -- true --> T4[throw IAE]
    G1 -- false --> G2{bloqueado?}
    G2 -- true --> R5[return RECUSADO]
    G2 -- false --> G3{comprasAnteriores == 0?}
    G3 -- true --> G4a{total > 100000?}
    G3 -- false --> G5a{total > 500000?}
    G4a -- true --> R6[return REVISAO]
    G4a -- false --> G4b{expresso?}
    G4b -- true --> R6
    G4b -- false --> R7[return APROVADO]
    G5a -- true --> G5b{!vip?}
    G5a -- false --> R7
    G5b -- true --> R8[return REVISAO]
    G5b -- false --> R7
    T4 --> END3((end))
    R5 --> END3
    R6 --> END3
    R7 --> END3
    R8 --> END3
```

`PagamentoService.pagar`:

```mermaid
flowchart TD
    S4((start)) --> H1{total <= 0?}
    H1 -- true --> T5[throw IAE]
    H1 -- false --> H2a{maxTentativas < 1?}
    H2a -- true --> T6[throw IAE]
    H2a -- false --> H2b{maxTentativas > 3?}
    H2b -- true --> T6
    H2b -- false --> LOOP[tentativa++]
    LOOP --> H3{autorizar lança ISE?}
    H3 -- "não: retorna true/false" --> R9[return resultado]
    H3 -- "sim: capturado" --> H4{tentativa < maxTentativas?}
    H3 -. "outra exceção não capturada" .-> END4((end))
    H4 -- true --> LOOP
    H4 -- false --> R10[return false]
    T5 --> END4
    T6 --> END4
    R9 --> END4
    R10 --> END4
```

Aqui tem uma aresta pontilhada saindo do `H3` direto pro `end`: é quando
estoura uma exceção diferente de `IllegalStateException` dentro do `try`.
Ela não vira decisão de código (não tem `if` pra ela), mas conta como
aresta na hora de somar `E - N + 2`, por isso o número não bate certinho
com "quantidade de decisões + 1" nesse método.

`PedidoService.fechar`:

```mermaid
flowchart TD
    S5((start)) --> I1{pedido == null?}
    I1 -- true --> T7[throw NPE]
    I1 -- false --> I2{cliente == null?}
    I2 -- true --> T8[throw NPE]
    I2 -- false --> I3{cliente.bloqueado?}
    I3 -- true --> R11[return BLOQUEADO]
    I3 -- false --> I4{subtotal == 0?}
    I4 -- true --> T9[throw IAE]
    I4 -- false --> I5{!estoqueSuficiente?}
    I5 -- true --> R12[return SEM_ESTOQUE]
    I5 -- false --> I6{analise != APROVADO?}
    I6 -- true --> R13[return resultado sem cobrança]
    I6 -- false --> I7{pagar retorna true?}
    I7 -- true --> R14[return PAGO]
    I7 -- false --> R15[return PAGAMENTO_RECUSADO]
    T7 --> END5((end))
    T8 --> END5
    R11 --> END5
    T9 --> END5
    R12 --> END5
    R13 --> END5
    R14 --> END5
    R15 --> END5
```

| Método | Nós | Arestas | V(G) | Caminhos independentes | Restrições de viabilidade |
| --- | --- | --- | --- | --- | --- |
| `PoliticaDesconto.calcular` | 23 | 33 | 12 | 12 | nenhuma |
| `CalculadoraFrete.calcular` | 22 | 31 | 11 | 11 | nenhuma |
| `AnaliseRisco.avaliar` | 14 | 20 | 8 | 8 | nenhuma |
| `PagamentoService.pagar` | 13 | 18 | 7 | 6 | a exceção não capturada soma uma aresta a mais do que o número de decisões (6) |
| `PedidoService.fechar` | 17 | 23 | 8 | 8 | o caminho SEM_ESTOQUE com cupom inválido só existe aqui porque a checagem de estoque vem antes da checagem do cupom; sozinho em `PoliticaDescontoTest` esse cupom daria erro |

## Matriz de testes

| ID / método JUnit | Unidade | Entrada e estado do stub | Resultado esperado | Caminho / aresta | Critério atendido |
| --- | --- | --- | --- | --- | --- |
| `clienteVipRecebeDezPorCento` | PoliticaDesconto | vip, subtotal 1000,00 | desconto 100,00 | ramo vip | classe de equivalência |
| `cupomDesconhecidoLancaExcecao` | PoliticaDesconto | cupom "NAOEXISTE" | IllegalArgumentException | default do switch | classe inválida |
| `descontoCombinadoRespeitaTetoDeVintePorCento` | PoliticaDesconto | vip + BEMVINDO, subtotal 100,00 | desconto capado em 20,00 | teto do desconto | condição composta |
| `baseParanaSemPesoExcedenteEValorBaixo` | CalculadoraFrete | UF PR, peso 1kg | frete 12,00 | ramo PR do switch | classe de equivalência |
| `semExcedenteNoLimiteExatoDeDoisQuilos` | CalculadoraFrete | peso 2000g | sem cobrança extra | 0 iterações do while | limite (0 iterações) |
| `freteGratuitoNaoSeAplicaAPedidoExpresso` | CalculadoraFrete | líquido 300,00, expresso | frete não zera | curto-circuito do `&&` | condição composta |
| `comComprasAnterioresClienteVipEAprovadoMesmoComTotalAlto` | AnaliseRisco | com histórico, total alto, vip | APROVADO | `!vip` falso no `&&` | curto-circuito |
| `deveEsgotarTentativasEDevolverFalsoQuandoSempreIndisponivel` | PagamentoService | ISE nas 3 tentativas | false, 3 chamadas | limite do laço | esgotamento de tentativas |
| `devePropagarExcecoesQueNaoSaoIndisponibilidadeTemporaria` | PagamentoService | RuntimeException | propaga | aresta não capturada | exceção não tratada |
| `deveRetornarSemEstoqueAntesDeAplicarOuValidarOCupom` | PedidoService | estoque insuficiente, cupom inválido | SEM_ESTOQUE, sem exceção | checagem de estoque antes do desconto | caminho inviável na unidade isolada |
| `deveFecharPedidoDeClienteComumComFreteDoParanaEPagamentoAprovado` | PedidoService | caminho completo, pagamento aprovado | PAGO | caminho feliz completo | colaboração entre classes |

As demais classes de teste cobrem validação de campo e os métodos de
agregação de `Pedido` (subtotal, peso, item frágil, estoque), que não
entraram na tabela de complexidade por serem métodos mais simples.

## Evolução da cobertura

| Etapa | Testes executados | Linhas | Branches | Métodos | Classes | Lacunas e justificativas |
| --- | --- | --- | --- | --- | --- | --- |
| Inicial | 1 | não medido | não medido | não medido | não medido | só tinha o teste de exemplo |
| Depois desta entrega | 88 (todos passando) | 108/108 (100%) | 116/116 (100%) | 21/21 (100%) | 9/9 (100%) | `mvn clean test` rodado com JaCoCo; nenhuma linha, ramo, método ou classe do pacote `pedidos` ficou de fora |

## Análise crítica

**Quais combinações faltavam mesmo com os ramos cobertos?**
Testar vip e expresso separados não garante que os dois juntos (com peso
excedente e item frágil) dão o valor certo, já que o frete é calculado em
cima do resultado anterior (zera, depois divide por vip, depois soma
expresso, depois soma frágil). Tem um teste específico só pra essa
combinação.

**Quais condições não foram avaliadas por causa do curto-circuito?**
No `AnaliseRisco`, se o total é menor ou igual a 500.000 o `!vip()` nem
chega a ser avaliado. No `PoliticaDesconto`, se o cupom é nulo o
`isBlank()` nem é chamado, e se o cliente já comprou antes a condição do
subtotal mínimo do BEMVINDO também não entra.

**Quais caminhos são inviáveis no serviço mas viáveis na unidade isolada?**
Um cupom desconhecido é válido pra testar `PoliticaDesconto` sozinha (dá
erro), mas dentro do `fechar` esse caminho nunca acontece se o pedido
também não tiver estoque, porque a checagem de estoque vem antes da
checagem do cupom.

**Como foram testadas as exceções e as quantidades de iteração?**
No `PagamentoService` a gente testou aprovação direta, indisponibilidade
temporária com nova tentativa, esgotamento das tentativas e uma exceção
diferente (que propaga sem ser tratada, e não conta como branch no
JaCoCo mesmo assim testamos). O laço do frete foi testado com 0, 1 e 2
frações de peso excedente.

**Qual alteração proposital foi detectada e foi desfeita?**
Trocamos o limite `total > 500_000` de `AnaliseRisco` pra `600_000` e rodamos
a suíte de novo. Só o `comComprasAnterioresETotalAltoSemVipVaiParaRevisao`
quebrou (esperava REVISAO e veio APROVADO), o que faz sentido porque é o
único teste que usa um total entre 500.000 e 600.000 pra esse caminho.
Voltamos o limite pra `500_000` e a suíte passou de novo, os 88 testes
verdes.
