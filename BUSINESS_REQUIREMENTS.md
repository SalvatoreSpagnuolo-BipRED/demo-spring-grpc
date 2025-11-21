# Requisiti di Business – Piattaforma Contratti, Clienti, Prodotti e Pricing

## 1. Contesto e panoramica di dominio

### 1.1 Scenario generale

Il sistema gestisce:

- L’anagrafica dei **clienti**.
- Il ciclo di vita dei **contratti** associati ai clienti, con l’elenco dei **prodotti** contrattualizzati.
- Le regole di **pricing** applicate a ciascuna tupla **(contratto, prodotto)**, basate su quantità di utilizzo (usage) e scaglioni di prezzo.

L’interazione principale avviene tramite un’API REST esposta da `console-api`, che orchestra chiamate gRPC verso tre servizi core:

- `customer-manager-core` per i clienti.
- `contract-manager-core` per i contratti e l’uso dei prodotti.
- `pricing-manager-core` per la definizione, associazione e calcolo dei prezzi.

### 1.2 Bounded context / servizi

- **Customer Manager** (`customer-manager`)
  - Gestisce l’entità Cliente (identità, stato, dati anagrafici di base).
  - Espone operazioni di creazione, lettura, aggiornamento, cancellazione e lista paginata.

- **Contract Manager** (`contract-manager`)
  - Gestisce il ciclo di vita dei contratti di un cliente.
  - Gestisce l’insieme di prodotti associati a ciascun contratto e la registrazione dell’uso (usage) per prodotto.
  - Espone operazioni di creazione, aggiornamento, attivazione/disattivazione, cancellazione e consultazione contratti.

- **Pricing Manager** (`pricing-manager`)
  - Definisce schemi di prezzo parametrizzati per **frequenza** (es. mensile/annuale) e **scaglioni di quantità** (brackets).
  - Gestisce l’associazione di questi schemi a prodotti specifici all’interno di contratti.
  - Calcola il prezzo totale per una data quantità di utilizzo (usage) sulla tupla (contratto, prodotto).

### 1.3 Obiettivi funzionali

- Tracciare in modo consistente:
  - chi è il cliente (Customer Manager),
  - quali contratti ha in essere e quali prodotti sono inclusi (Contract Manager),
  - quali schemi di prezzo sono applicati a ciascun prodotto di ciascun contratto (Pricing Manager),
  - quale importo economico risulta da uno specifico utilizzo (usage) per un certo periodo.

---

## 2. Requisiti per bounded context / servizio

### 2.1 Customer Manager

#### 2.1.1 Responsabilità principali

- Gestire il **ciclo di vita del Cliente**, comprendendo:
  - identificazione univoca (`id`),
  - dati anagrafici di base (es. nome, eventuali contatti),
  - stato del cliente,
  - informazioni temporali di creazione e aggiornamento.

#### 2.1.2 Operazioni esposte e requisiti di business

1. **Creazione cliente** – `CreateCustomer(CreateCustomerRequest) -> CustomerResponse`
   - Il sistema deve consentire la creazione di un nuovo cliente fornendo almeno il **nome**.
   - Alla creazione devono essere garantiti:
     - generazione di un identificativo univoco `id`;
     - impostazione automatica dei timestamp `created_at` e `updated_at` alla data/ora corrente;
     - impostazione di uno stato iniziale coerente (tipicamente ACTIVE, da confermare a livello business).
   - Campi anagrafici aggiuntivi (es. email, telefono) possono essere completati successivamente.

2. **Recupero cliente** – `GetCustomer(GetCustomerRequest) -> CustomerResponse`
   - Il sistema deve permettere di recuperare i dati di un cliente a partire dal suo `id`.
   - Devono essere restituiti almeno:
     - dati anagrafici di base (nome, contatti se presenti),
     - stato corrente del cliente,
     - `created_at` e `updated_at`.

3. **Aggiornamento cliente** – `UpdateCustomer(UpdateCustomerRequest) -> CustomerResponse`
   - Il sistema deve consentire l’aggiornamento dei dati anagrafici di un cliente esistente.
   - Requisiti:
     - l’`id` del cliente rimane immutato;
     - `updated_at` viene aggiornato ad ogni modifica;
     - devono essere preservate le regole di consistenza sui campi (es. formati email/telefono, se presenti nell’implementazione).

4. **Cancellazione cliente** – `DeleteCustomer(DeleteCustomerRequest) -> DeleteCustomerResponse`
   - Il sistema deve consentire la cancellazione di un cliente identificato da `id`.
   - L’esito deve essere esplicito tramite i campi `success` (boolean) e `message` (descrizione).
   - Requisiti funzionali impliciti da chiarire con il business:
     - gestione di clienti con contratti attivi (probabile vincolo di non cancellabilità o di cancellazione solo logica / cambio stato).

5. **Lista clienti paginata** – `ListCustomers(ListCustomersRequest) -> ListCustomersResponse`
   - Il sistema deve supportare l’elenco dei clienti in forma paginata tramite i parametri `page` e `page_size`.
   - La risposta deve contenere:
     - la lista di `customers` della pagina corrente,
     - il totale complessivo `total` dei clienti coerente con i criteri (es. esclusi eventuali cancellati logici).

#### 2.1.3 Regole di business e invarianze

- Ogni cliente ha uno **stato** appartenente a un insieme finito (`ACTIVE`, `INACTIVE`, `SUSPENDED`).
  - Invariante: un solo stato valido alla volta.
- `created_at` e `updated_at` devono essere sempre valorizzati per clienti esistenti.
  - `updated_at` non può essere anteriore a `created_at`.
- L’`id` del cliente è una chiave stabile nel tempo.
- Alcune operazioni in altri domini (es. creazione o attivazione contratto) sono plausibilmente consentite solo se il cliente è in stato **ACTIVE**.

---

### 2.2 Contract Manager

#### 2.2.1 Responsabilità principali

- Gestire il **ciclo di vita dei contratti** associati ai clienti.
- Gestire l’insieme dei **prodotti contrattualizzati** per ogni contratto.
- Tracciare il **periodo di validità** del contratto (data inizio/fine) e il suo **stato**.
- Registrare il **consumo/uso (usage)** per prodotto nell’ambito di un contratto.

#### 2.2.2 Operazioni esposte e requisiti di business

1. **Creazione contratto** – `CreateContract(CreateContractRequest) -> ContractCodeResponse`
   - Il sistema deve permettere la creazione di un contratto per un cliente esistente, specificando:
     - `customer_id` del cliente titolare del contratto;
     - l’elenco di prodotti (`products`), ognuno con `product_id` e `pricing_id` associato;
     - `start_date` e `end_date` del periodo di validità.
   - Alla creazione devono essere garantiti:
     - generazione di un codice contratto `code` univoco;
     - assegnazione di una `version` iniziale (versione del contratto).
   - Regole implicite:
     - `customer_id` deve identificare un cliente esistente, tipicamente in stato ACTIVE;
     - devono essere rispettate le regole di coerenza temporale (`start_date` ≤ `end_date`);
     - ogni `product_id` deve essere coerente con un catalogo prodotti applicativo (non descritto nei `.proto` ma suggerito dalla semantica);
     - il campo `pricing_id` collega il prodotto a un particolare schema di prezzo definito nel dominio Pricing.

2. **Aggiornamento contratto** – `UpdateContract(UpdateContractRequest) -> ContractCodeResponse`
   - Il sistema deve consentire l’aggiornamento di un contratto esistente, comprendendo:
     - modifica dello **stato** (`ContractStatus`),
     - modifica dell’elenco dei **prodotti** associati (aggiunta/rimozione/sostituzione),
     - modifica del **periodo di validità** (`start_date`, `end_date`).
   - Ogni aggiornamento produce un nuovo assetto del contratto identificabile da `code` e una `version` aggiornata.
   - Invarianze attese:
     - lo stesso `code` può avere più versioni nel tempo;
     - non è consentito creare versioni con intervalli di validità incoerenti.

3. **Recupero dettagli contratto** – `GetContract(ContractCodeRequest) -> ContractDetailResponse`
   - Il sistema deve permettere di recuperare i dettagli di un contratto a partire da `code` e `version`.
   - La risposta deve includere almeno:
     - `customer_id` del titolare,
     - `status` del contratto,
     - l’elenco di `products` con `product_id` e `pricing_id`,
     - `start_date` e `end_date`.

4. **Cancellazione contratto** – `DeleteContract(ContractCodeRequest) -> EmptyResponse`
   - Il sistema deve consentire la cancellazione di un contratto identificato da `code` e `version`.
   - La risposta deve indicare esplicitamente:
     - se l’operazione è andata a buon fine (`success`),
     - un `message` descrittivo di dettaglio.
   - Vincoli impliciti da definire:
     - presenza di usage già registrato,
     - stato del contratto (es. contratti ACTIVE con usage non cancellabili fisicamente ma eventualmente solo disattivabili).

5. **Lista contratti** – `ListContract(google.protobuf.Empty) -> ListContractsResponse`
   - Il sistema deve permettere di ottenere un elenco di contratti (`ContractDetailResponse`).
   - Dalla definizione gRPC non risultano filtri o paginazione, ma a livello business ci si può attendere filtri (es. per cliente, stato) lato REST/orchestrazione.

6. **Attivazione contratto** – `ActivateContract(ContractCodeRequest) -> EmptyResponse`
   - Il sistema deve consentire il passaggio del contratto in stato **ACTIVE**.
   - Condizioni funzionali tipiche (implicite):
     - la data corrente deve ricadere nel periodo `[start_date, end_date]`;
     - il cliente associato deve essere in stato compatibile (es. ACTIVE);
     - non devono esistere conflitti con altri contratti incompatibili sullo stesso perimetro (da definire col business).

7. **Disattivazione contratto** – `DeactivateContract(ContractCodeRequest) -> EmptyResponse`
   - Il sistema deve consentire la disattivazione del contratto (es. passaggio a stato INACTIVE).
   - Possibile scenario: cessazione anticipata rispetto a `end_date` o blocco per morosità / condizioni contrattuali.

8. **Registrazione usage per prodotto di contratto** – `AddUsageToContractProduct(AddUsageToContractProductRequest) -> EmptyResponse`
   - Il sistema deve consentire di registrare un valore di **usage** per un determinato prodotto all’interno di un contratto, specificando:
     - `contract_code`,
     - `product_id`,
     - `usage_amount` (quantità utilizzata).
   - Regole funzionali implicite:
     - il contratto deve esistere ed essere in stato compatibile (tipicamente ACTIVE);
     - il `product_id` deve essere realmente associato al contratto;
     - `usage_amount` deve essere non negativo;
     - gli usage registrati sono presumibilmente utilizzati per calcolo prezzi, reportistica o fatturazione.

#### 2.2.3 Regole di business e invarianze

- Identificazione contratto:
  - `code` + `version` identificano univocamente una specifica versione di contratto.
- Stato contratto:
  - `status` appartiene a un insieme finito (`ACTIVE`, `INACTIVE`, `PENDING`).
  - Ciclo di vita tipico: `PENDING` → `ACTIVE` → `INACTIVE`.
- Validità temporale:
  - Devono essere garantite le condizioni `start_date` ≤ `end_date`.
  - Per i contratti ACTIVE la data corrente dovrebbe ricadere nel periodo di validità.
- Prodotti del contratto:
  - Ogni `Product` è descritto almeno da `product_id` e `pricing_id`.
  - Per semplicità funzionale si può assumere che per una stessa versione di contratto e per uno stesso `product_id` sia attivo al più un solo `pricing_id`.

---

### 2.3 Pricing Manager

#### 2.3.1 Responsabilità principali

- Gestire **schemi di prezzo** (price list) caratterizzati da:
  - una **frequenza** (es. canone mensile/annuale),
  - una o più **fasce di quantità (brackets)** con prezzi unitari differenziati.
- Associare uno schema di prezzo a una specifica tupla **(contratto, prodotto)**.
- Calcolare il **prezzo totale** in base alla quantità di utilizzo (usage) e allo schema di prezzo associato.

#### 2.3.2 Operazioni esposte e requisiti di business

1. **Creazione schema di prezzo** – `CreatePrice(CreatePriceRequest) -> PriceCodeResponse`
   - Il sistema deve permettere la creazione di un nuovo schema di prezzo specificando:
     - `period` (frequenza) tramite `PricingFrequency` (es. MONTHLY, YEARLY),
     - una lista di `pricing_brackets` (fasce di quantità).
   - Ogni `Bracket` include almeno:
     - `order`: posizione/ordinamento,
     - `min_quantity`,
     - `max_quantity` opzionale (fascia potenzialmente aperta verso l’alto),
     - `unit_price`.
   - Alla creazione deve essere garantita l’assegnazione di un codice univoco `code` per identificare lo schema di prezzo.
   - Invarianze attese sulle fasce:
     - le fasce non devono sovrapporsi;
     - `min_quantity` < `max_quantity` quando `max_quantity` è presente;
     - l’insieme delle fasce deve coprire il dominio di quantità di interesse (es. da 0 in poi) secondo le regole definite.

2. **Aggiornamento schema di prezzo** – `UpdatePrice(UpdatePriceRequest) -> PriceCodeResponse`
   - Il sistema deve consentire di modificare uno schema di prezzo esistente mantenendo il suo `code`, aggiornando:
     - `period`,
     - l’insieme delle `pricing_brackets`.
   - Questo permette di evolvere le regole di pricing nel tempo mantenendo la stessa identità logica dello schema.

3. **Recupero schema di prezzo** – `GetPrice(PriceCodeRequest) -> PriceResponse`
   - Il sistema deve permettere di consultare uno schema di prezzo esistente a partire dal suo `code`.
   - La risposta deve restituire almeno `period` e la lista completa delle `pricing_brackets`.

4. **Cancellazione schema di prezzo** – `DeletePrice(PriceCodeRequest) -> EmptyResponse`
   - Il sistema deve permettere di cancellare uno schema di prezzo tramite il suo `code`.
   - La risposta deve indicare esplicitamente esito (`success`) e un `message` descrittivo.
   - Vincoli impliciti plausibili:
     - evitare la cancellazione di schemi di prezzo ancora associati a contratti/prodotti attivi, preferendo strategie di disattivazione o versioning.

5. **Lista schemi di prezzo** – `ListPrice(google.protobuf.Empty) -> PriceListResponse`
   - Il sistema deve permettere di ottenere l’elenco degli schemi di prezzo esistenti.
   - Non sono previsti filtri/paginazione a livello gRPC; questi possono essere implementati a livello superiore.

6. **Associazione prezzo a prodotto di contratto** – `AssociatePriceToContractProduct(AssociatePriceToContractProductRequest) -> EmptyResponse`
   - Il sistema deve consentire di associare uno schema di prezzo (`price_code`) a uno specifico prodotto (`product_code`) in uno specifico contratto (`contract_code`).
   - Requisiti funzionali:
     - il contratto identificato da `contract_code` deve esistere;
     - il prodotto (`product_code`) deve essere incluso tra i prodotti del contratto;
     - il prezzo (`price_code`) deve essere uno schema valido e disponibile.
   - Invarianze implicite:
     - per una stessa tupla (contratto, prodotto) deve essere attivo al più uno schema di prezzo alla volta.

7. **Calcolo prezzo** – `CalculatePrice(CalculatePriceRequest) -> CalculatePriceResponse`
   - Il sistema deve calcolare l’importo dovuto per una combinazione:
     - `contract_code`,
     - `product_code`,
     - `quantity` (quantità di utilizzo).
   - La risposta deve includere:
     - `contract_code` e `product_code` (echo),
     - `period` di applicazione del prezzo,
     - la `pricing_bracket` effettivamente utilizzata per il calcolo,
     - `total_price` risultante.
   - Regole di business implicite:
     - la `quantity` deve ricadere in una delle fasce (`Bracket`) definite nello schema associato a quella tupla (contratto, prodotto);
     - il sistema deve determinare la fascia corretta in base a `min_quantity`/`max_quantity` e calcolare `total_price` secondo la regola implementata (tipicamente `unit_price * quantity`);
     - deve esistere una associazione valida tra contratto, prodotto e schema di prezzo al momento del calcolo.

#### 2.3.3 Modello dati e regole

- **PricingFrequency** definisce la granularità temporale del prezzo (tipicamente `MONTHLY`, `YEARLY`).
- **Bracket** modella i prezzi a scaglioni:
  - fasce con limiti `min_quantity` e `max_quantity` opzionale;
  - `unit_price` differenziato per fascia;
  - `order` per mantenere un ordinamento deterministico.

---

## 3. Regole di business trasversali e invarianze

### 3.1 Coerenza tra domini

- Ogni `customer_id` presente nei contratti deve riferirsi a un cliente esistente gestito da **Customer Manager**.
- I riferimenti a `contract_code` e `product_id`/`product_code` tra **Contract Manager** e **Pricing Manager** devono essere consistenti:
  - il prodotto indicato in `AssociatePriceToContractProduct` deve essere incluso tra i prodotti del contratto;
  - `pricing_id` nei prodotti del contratto deve essere coerente con i `price_code` gestiti da Pricing.

### 3.2 Stati e cicli di vita

- **Cliente**
  - Stati: almeno `ACTIVE`, `INACTIVE`, `SUSPENDED`.
  - Operazioni quali creazione o attivazione contratto sono plausibilmente consentite solo per clienti ACTIVE.

- **Contratto**
  - Stati: almeno `PENDING`, `ACTIVE`, `INACTIVE`.
  - Transizioni tipiche:
    - `PENDING` → `ACTIVE` (attivazione);
    - `ACTIVE` → `INACTIVE` (disattivazione o cessazione).
  - Per contratti non ACTIVE possono essere vietate operazioni come registrazione usage e calcolo prezzi (regola attesa ma da confermare).

- **Pricing**
  - Gli schemi di prezzo possono essere creati, aggiornati e cancellati, ma il sistema deve garantire che i calcoli facciano sempre riferimento a schemi **validi** e **coerenti** rispetto al momento e al contesto di utilizzo.

### 3.3 Integrità dei riferimenti

- Il sistema deve evitare la creazione di riferimenti orfani:
  - non devono essere associati contratti a clienti inesistenti;
  - non devono essere associati prezzi a (contratto, prodotto) inesistenti;
  - non devono essere cancellati clienti, contratti o schemi di prezzo ancora necessari per mantenere la consistenza del dominio (es. contratti attivi, calcoli storici).

### 3.4 Tracciabilità e audit

- Tramite `created_at`, `updated_at`, `version` di contratto e codici di prezzo, il sistema deve consentire di ricostruire:
  - quando un cliente/contratto/prezzo è stato creato o modificato;
  - quale versione di contratto era in vigore in un dato momento;
  - quali regole di prezzo erano applicate a un prodotto in un certo intervallo temporale.

---

## 4. Flussi tipici (use case emergenti)

### 4.1 Onboarding cliente e creazione contratto

1. **Creazione cliente**
   - Operazione: `CustomerService.CreateCustomer`.
   - Obiettivo: registrare un nuovo cliente con almeno il nome e ottenere l’`id` assegnato.

2. **Arricchimento dati cliente (opzionale)**
   - Operazione: `CustomerService.UpdateCustomer`.
   - Obiettivo: completare/aggiornare le informazioni anagrafiche.

3. **Creazione contratto per il cliente**
   - Operazione: `ContractService.CreateContract` con `customer_id`, elenco `products`, `start_date`, `end_date`.
   - Risultato: creazione di un nuovo contratto identificato da `code` e `version`.

4. **Attivazione del contratto**
   - Operazione: `ContractService.ActivateContract` con `ContractCodeRequest`.
   - Condizione: cliente e contratto devono soddisfare le regole di validità e stato.

### 4.2 Definizione pricing e associazione a prodotti di contratto

1. **Definizione schema di prezzo**
   - Operazione: `PricingService.CreatePrice`.
   - Obiettivo: creare uno schema di prezzo con frequenza (`period`) e scaglioni (`pricing_brackets`).

2. **Revisione schema di prezzo (opzionale)**
   - Operazione: `PricingService.UpdatePrice`.
   - Obiettivo: adattare nel tempo i prezzi o la struttura delle fasce mantenendo lo stesso `code`.

3. **Associazione prezzo a prodotto di contratto**
   - Operazione: `PricingService.AssociatePriceToContractProduct`.
   - Obiettivo: collegare un `price_code` a un prodotto specifico (`product_code`) di un contratto (`contract_code`), rendendo possibile il calcolo prezzo per quella tupla.

### 4.3 Registrazione usage e calcolo prezzo

1. **Registrazione usage per prodotto di contratto**
   - Operazione: `ContractService.AddUsageToContractProduct`.
   - Obiettivo: registrare la quantità di utilizzo (`usage_amount`) di un prodotto all’interno di un contratto.

2. **Calcolo del prezzo in base all’uso**
   - Operazione: `PricingService.CalculatePrice` con `contract_code`, `product_code`, `quantity`.
   - Obiettivo: determinare l’importo dovuto per quella quantità, indicando anche la fascia (`pricing_bracket`) e il `period`.

### 4.4 Gestione ciclo di vita e dismissione

1. **Modifica stato cliente**
   - Operazioni: `UpdateCustomer` (e/o logiche interne).
   - Obiettivo: riflettere cambi di stato del cliente (es. sospensione), con impatto sulle operazioni possibili sui contratti.

2. **Disattivazione contratto**
   - Operazione: `ContractService.DeactivateContract`.
   - Obiettivo: cessare la validità operativa del contratto (es. fine rapporto, rescissione).

3. **Cancellazione entità**
   - Clienti: `CustomerService.DeleteCustomer`.
   - Contratti: `ContractService.DeleteContract`.
   - Pricing: `PricingService.DeletePrice`.
   - Obiettivo: rimuovere entità non più necessarie, rispettando i vincoli di integrità e storicizzazione.

---

## 5. Punti da chiarire con il business

- **Gestione completa del dominio Prodotti**
  - I `.proto` usano `product_id`/`product_code`, ma non è definito un dominio esplicito dei prodotti.
  - Da chiarire: ciclo di vita del prodotto, attributi, regole di compatibilità con clienti/contratti.

- **Matrici di stato e regole di transizione**
  - Formalizzare in modo esplicito quali operazioni sono permesse in quali stati per clienti, contratti e prezzi.

- **Politiche di cancellazione (soft vs hard delete)**
  - Definire le regole per la cancellazione di clienti, contratti e schemi di prezzo in presenza di storico, usage o associazioni attive.

- **Versioning e impatto sui calcoli storici**
  - Chiarire come gestire contratti multi-versione e aggiornamenti degli schemi di prezzo, in relazione ai calcoli storici (es. fatture passate):
    - se devono usare sempre le regole vigenti al momento dell’uso,
    - oppure se devono essere ricalcolati con le regole più recenti (tipicamente no, ma da esplicitare).

