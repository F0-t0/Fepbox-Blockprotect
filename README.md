# Fepbox-BlockProtect

Zaawansowany, wydajny i w pełni konfigurowalny plugin do ochrony bloków dla **Paper 1.21.4** (kompatybilny ze Spigot API tam, gdzie to możliwe).

Plugin pozwala zdefiniować zbiór reguł, które określają:
- **jakie bloki** są chronione (materiały lub grupy),
- **gdzie** obowiązuje ochrona (GLOBAL / WORLD / CUBOID / RADIUS / LOCATIONS),
- **przed czym** chroni (break, explosion, piston, fire, fluids),
- **kto może ominąć ochronę** (bypass przez permisje, gamemode, UUID, narzędzie).

Jedyny komunikat dla gracza przy próbie zniszczenia chronionego bloku:

> `Nie możesz zniszczyć tego bloku`

Brak actionbara, tytułów, dźwięków, particle, cooldownów, webhooków itp.

---

## Wymagania

- **Java 21**
- **Paper 1.21.4** (lub forki kompatybilne z tą wersją API)
- System budowania: **Maven**

---

## Instalacja (serwer)

1. Zbuduj plugin (sekcja „Budowanie” poniżej) lub pobierz gotowy JAR.
2. Skopiuj plik:
   ```text
   target/fepbox-blockprotect-1.0.0.jar
   ```
   do katalogu `plugins/` na serwerze Paper.
3. Uruchom lub zrestartuj serwer.
4. Po pierwszym starcie wygeneruje się `config.yml` w katalogu pluginu.

---

## Budowanie (Maven)

W katalogu głównym projektu:

```bash
mvn clean package
```

Wynikowy JAR znajdziesz w:

```text
target/fepbox-blockprotect-1.0.0.jar
```

---

## Funkcjonalność

### Zdarzenia blokowane

Plugin potrafi zablokować zniszczenie / zmianę chronionych bloków przez:

- `BlockBreakEvent` – ręczne kopanie (gracz).
- `EntityExplodeEvent`, `BlockExplodeEvent` – eksplozje (TNT, creepery, łańcuch TNT).
- `BlockPistonExtendEvent`, `BlockPistonRetractEvent` – przesuwanie przez tłoki.
- `BlockBurnEvent`, `BlockIgniteEvent` – ogień.
- `BlockFromToEvent` – przepływ wody / lawy.
- `EntityChangeBlockEvent` (FALLING_BLOCK) – piasek / żwir nie spadają z i na chronione bloki.

### Zakres ochrony (scope)

Każda reguła ma własny zakres działania:

- `GLOBAL` – wszystkie światy.
- `WORLD` – wybrane światy.
- `CUBOID` – zaznaczony cuboid (pos1 / pos2).
- `RADIUS` – promień od punktu.
- `LOCATIONS` – konkretne współrzędne bloków.

Zakres definiowany jest w sekcji `scope` w `config.yml`.

### Definicja chronionych bloków

Bloki można definiować na 3 sposoby:

- pojedynczy `Material`, np.:
  ```yaml
  blocks:
    - DIAMOND_BLOCK
  ```
- grupy zdefiniowane w `groups`:
  ```yaml
  blocks:
    - "#valuable_blocks"
  ```
- mieszanie obu podejść:
  ```yaml
  blocks:
    - STONE
    - "#glass"
  ```

Grupy deklarujemy w:

```yaml
groups:
  valuable_blocks:
    - DIAMOND_BLOCK
    - EMERALD_BLOCK
  glass:
    - GLASS
    - GLASS_PANE
```

### Flagi ochrony

Każda reguła ma osobne flagi:

```yaml
flags:
  break: true      # ochrona przed kopaniem
  explosion: true  # ochrona przed wybuchami
  piston: true     # ochrona przed tłokami
  fire: true       # ochrona przed ogniem
  fluids: true     # ochrona przed płynami
```

Przykłady:

- blok niezniszczalny ręką, ale podatny na TNT:
  ```yaml
  flags:
    break: true
    explosion: false
    piston: false
    fire: false
    fluids: false
  ```
- blok odporny tylko na tłoki:
  ```yaml
  flags:
    break: false
    explosion: false
    piston: true
    fire: false
    fluids: false
  ```

### By-pass (wyjątki)

Sekcja `bypass` decyduje, kto może zniszczyć chroniony blok bez komunikatu:

```yaml
bypass:
  permissions:
    - fepbox.blockprotect.bypass
  gamemodes:
    - CREATIVE
  players:
    - "uuid-gracza-1"
  tools:
    - NETHERITE_PICKAXE
```

Jeśli gracz spełnia co najmniej jeden warunek bypass:
- blok może zostać zniszczony,
- **nie** otrzymuje żadnego komunikatu.

Bypass jest stosowany tylko tam, gdzie jest gracz (np. `BlockBreakEvent`). Dla eksplozji / płynów nie ma kontekstu gracza, więc bypass nie jest brany pod uwagę.

---

## Komendy

Główna komenda: `/bp`

### `/bp wand`

- Daje różdżkę do zaznaczania cuboida (`BLAZE_ROD` z nazwą `BlockProtect Wand`).
- Kliknięcia:
  - **LPM** w blok – ustawia `pos1`.
  - **PPM** w blok – ustawia `pos2`.
- Wymagane uprawnienie: `fepbox.blockprotect.admin`.

### `/bp createcuboid <id>`

- Tworzy lub nadpisuje regułę `CUBOID` o podanym `id`, używając aktualnego zaznaczenia (`pos1`, `pos2`).
- Świat jest brany z aktualnego świata gracza.
- Domyślnie:
  - wszystkie flagi `true`,
  - pusta lista bloków (dodajesz później `/bp addruleblock`),
  - brak bypassów.
- Wymagane: `fepbox.blockprotect.admin`.

### `/bp addruleblock <id> <MATERIAL|#group>`

- Dodaje materiał lub grupę do listy bloków reguły.
  - `MATERIAL` – nazwa z enum `Material`, np. `STONE`, `DIAMOND_BLOCK`.
  - `#group` – odwołanie do grupy z sekcji `groups`.
- Wymagane: `fepbox.blockprotect.admin`.

### `/bp removeruleblock <id> <MATERIAL|#group>`

- Usuwa materiał lub grupę z listy bloków reguły.
- Wymagane: `fepbox.blockprotect.admin`.

### `/bp setflag <id> <break|explosion|piston|fire|fluids> <true|false>`

- Ustawia konkretną flagę dla reguły.
- Wymagane: `fepbox.blockprotect.admin`.

### `/bp delete <id>`

- Usuwa regułę o podanym `id`.
- Wymagane: `fepbox.blockprotect.admin`.

### `/bp list`

- Wyświetla listę wszystkich reguł:
  - ID, typ scope, liczba materiałów w regule.
- Brak specjalnych uprawnień (ale warto ograniczyć do adminów przez permisje komendy, jeśli chcesz).

### `/bp reload`

- Przeładowuje `config.yml` z dysku i odbudowuje cache reguł.
- Wymagane: `fepbox.blockprotect.reload`.

---

## Uprawnienia

Zdefiniowane w `plugin.yml`:

- `fepbox.blockprotect.admin`
  - Dostęp do komend administracyjnych: `wand`, `createcuboid`, `addruleblock`, `removeruleblock`, `setflag`, `delete`, `list`.
  - Domyślnie: `op`.
- `fepbox.blockprotect.reload`
  - Dostęp do `/bp reload`.
  - Domyślnie: `op`.
- `fepbox.blockprotect.bypass`
  - Może omijać ochronę, gdy reguła ma ten permission w sekcji `bypass.permissions`.
  - Domyślnie: `false`.

---

## Przykładowa konfiguracja

```yaml
groups:
  valuable_blocks:
    - DIAMOND_BLOCK
    - EMERALD_BLOCK
  glass:
    - GLASS
    - GLASS_PANE

rules:
  global_valuables:
    scope:
      type: GLOBAL
    blocks:
      - "#valuable_blocks"
    flags:
      break: true
      explosion: true
      piston: true
      fire: true
      fluids: true
    bypass:
      permissions:
        - fepbox.blockprotect.bypass
      gamemodes:
        - CREATIVE
      players: []
      tools: []
```

---

## Wydajność

- Reguły są parsowane i cache’owane w pamięci podczas startu pluginu oraz `/bp reload`.
- Materiały z `#group` są rozwijane do `Set<Material>` (szybkie `contains`).
- Zakresy (CUBOID, RADIUS, LOCATIONS) są przechowywane w zoptymalizowanej formie (granice, promień², zakodowane lokacje).
- Dla każdego świata utrzymywana jest lista reguł, aby ograniczyć liczbę sprawdzanych wpisów.
- Brak iteracji po chunkach, brak tasków async, brak ciężkich operacji w event handlerach.

---

## Licencja

Brak jawnie określonej licencji – dostosuj do potrzeb projektu (np. MIT / GPL) i zaktualizuj ten plik, jeśli chcesz upublicznić plugin. 

