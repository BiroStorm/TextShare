# TEXTSHARE APP
---
## Progetto di Laboratorio di Sistemi Operativi

#### Università di Bologna | Informatica per il Management | A.A. 2021-2022

[![MIT License](https://img.shields.io/badge/License-MIT-green.svg)](https://choosealicense.com/licenses/mit/)

----

## Specifiche e Documentazione
Si è richiesto di progettare un file server che permetta a più utenti di connettersi contemporaneamente per condividere, visualizzare e modificare file testuali. Il file server resta in esecuzione e custodisce i veri e propri file su disco. Gli utenti del servizio accedono ai suddetti file dalla propria macchina attraverso un’applicazione client che comunica col server e che permette loro di:

- Creare nuovi file
- Leggere i file esistenti
- Modificare e rinominare i file esistenti
- Eliminare i file esistenti

Quando un utente richiede di leggere un file, il server apre una _sessione di lettura_ per quel file. Quando un utente richiede di modificare un file, viene aperta una _sessione di scrittura_ per quel file.
L’apertura delle _sessioni di lettura_ e _scrittura_ rispetta delle regole fondamentali:
un numero arbitrario di utenti può leggere lo stesso file contemporaneamente, ma finché c’è almeno un utente che sta leggendo il file nessuno può modificare lo stesso file. Nell’altro verso, quando un utente sta modificando un file, nessuno può leggere o modificare lo stesso file. Ovviamente, nessun file può essere rinominato o eliminato mentre qualcuno lo sta leggendo o modificando.
Se un utente richiede un’operazione (e.g. leggere un file) in un momento in cui non può essere portata a termine (e.g. perché un altro utente sta modificando lo stesso file) non viene rifiutato dal server, bensì rimane in attesa finché non è possibile soddisfare la sua richiesta (e.g. finché la sessione di scrittura non viene conclusa).

Per le altre specifiche consultare le [Speficihe di Progetto](Specifiche_di_progetto_2021_22.pdf), mentre per la documentazione è possibile consultare direttamente il [PDF allegato al progetto](DOCUMENTAZIONE.pdf).

---
## Authors

- [Andrea Serrano](mailto:andrea.serrano2@studio.unibo.it)
- [Enjun Hu](https://github.com/BiroStorm)
- [Filippo Berveglieri](mailto:filippo.berveglieri@studio.unibo.it)
- [Gabriele Centonze](https://it.linkedin.com/in/gabriele-centonze-122161187)


---
## Acknowledgements

Si ringrazia il Prof. [Andrea Colledan](mailto:andrea.colledan@unibo.it) per le ore dedicate, per questa esperienza di gruppo e aver dato il nulla osta per la pubblicazione di questo progetto.
Ovviamente un ringraziamento speciale a tutti i membri del Team che hanno contribuito a realizzare questo progetto.


