## Wyższa Szkoła Informatyki Stosowanej i Zarządzania

# ALGORYTMY PRZETWARZANIA OBRAZÓW

## Aplikacja do obróbki obrazu za pomocą operacji jednopunktowych, sąsiedztwa.

Autor:
Łukasz Bany


## 1. Wprowadzenie

Aplikacja służy do obróbki obrazu za pomocą operacji jednopunktowych i sąsiedztwa.
Dodatkowym zakresem projektu jest udoskonalenie oprogramowania przygotowanego na
zajęciach poprzez:

- rozwinięcie prezentacji histogramu tak, aby opisywał wskazane fragmenty histogramu i
    regulował odcięciem skrajnych pikseli;
- rozwinięcie operacji sąsiedztwa o wybór strategii uzupełniania marginesów i normalizacji
    zakresu poziomów szarości

## 1.1. Wymagania systemowe

System operacyjny Windows i środowisko uruchomieniowe Java 8.

## 1.2. Uruchomienie programu

Program uruchamia się poprzez uruchomienie pliku Pikasso.exe w katalogu programu.

## 1.3. Wykorzystane narzędzia

Program został napisany w języku Java 8 z wykorzystaniem biblioteki JavaFX do budowy
interfejsu graficznego. Ponadto użyto:

- Apache Maven – do budowania projektu i importu bibliotek
- Apache Commons Math3 – do operacji matematycznych
- JUnit – testy jednostkowe
- Java Advanced Imaging – otwieranie plików TIF
- OpenCV – operacje na obrazach
- ControlsFX – dodatkowa biblioteka z komponentami dla JavaFX
- Lombok – biblioteka ułatwiająca pisanie oprogramowania

## 2. Podstawowe operacje

## 2.1. Otwarcie pliku

Aby otworzyć plik, należy wybrać z menu Plik opcję otwórz lub naciśnięcie Ctrl + O i wybranie pliku w otwartym oknie. Obsługiwane formaty plików to:

- jpg
- jpeg
- bmp
- png
- tif

Możliwe jest również otwarcie pliku poprzez jego przeciągnięcie do okna programu.


## 2.2. Zapisanie pliku

Zapisanie pliku odbywa się poprzez wybranie z menu Plik opcji Zapisz jako lub poprzez kombinację Ctrl + S oraz wybranie nazwy i rozszerzenia pliku. Obsługiwane typy plików przy zapisie:

- jpg
- bmp
- png

## 2.3. Zamknięcie pliku

Zamknięcie pliku odbywa się poprzez wybranie z menu Plik opcji Zamknij plik.

## 2.4. Zamknięcie programu

Zamknięcie programu odbywa się poprzez menu Plik -> Zamknij program (lub naciśnięcie krzyżyka w prawym górnym rogu ekranu).

## 2.5. Cofnięcie ostatniej zmiany

Ostatnią dokonaną na obrazie zmianę można cofnąć poprzez wybranie z menu Edycja opcji Cofnij (lub poprzez naciśnięcie Ctrl + Z). Możliwe jest cofnięcie się o 1 krok.


## 3. Narzędzia

## 3.1. Histogram obrazu

Podgląd histogramu obrazu można włączyć poprzez opcję menu Obraz - > Histogram.

Po użyciu opcji na ekranie pojawi się nowe okno Histogram:

Dla obrazów szaro-odcieniowych domyślnie wyświetlają się jedynie poziomy szarości. Dla obrazów kolorowych kanały czerwony, zielony i niebieski. Po lewej stronie okna wyświetlają się statystyki poszczególnych kanałów. Jest tu także możliwość sterowania widocznością kanałów. Przykładowo, aby włączyć widoczność kanału czerwonego, należy zaznaczyć pole „kanał czerwony”.


## 3.2. Histogram fragmentu obrazu

Istnieje możliwość podglądu histogramu i statystyk jedynie dla fragmentu obrazu. W tym celu należy zaznaczyć fragment obrazu za pomocą myszki i otworzyć okno histogramu poprzez opcję Obraz -> Histogram (lub jeśli jest już otwarte klikając przycisk „odśwież” w oknie Histogram. Statystyki dotyczące kanałów zostaną odświeżone dla zaznaczonego fragmentu.

## 3.3. Odcięcie skrajnych pikseli histogramu

Aby odciąć skrajne piksele obrazu, należy zaznaczyć interesujący nas zakres na pasku zakresu i kliknąć przycisk „odśwież”. Statystyki dotyczące kanałów zostaną odświeżone dla podanego zakresu.


## 3.4. Rozciągnięcie histogramu

Aby przeprowadzić operację rozciągnięcia histogramu należy nacisnąć przycisk „rozciągnij histogram” (lewy dolny róg ekranu). Pojawi się okno podglądu, w którym należy potwierdzić operację przyciskiem „Zachowaj”.

Histogram przed i po operacji:


## 3.5. Wyrównanie histogramu

Aby przeprowadzić operację wyrównania histogramu należy nacisnąć przycisk „wyrównaj histogram” (lewy dolny róg ekranu). Pojawi się okno podglądu, w którym należy potwierdzić operację przyciskiem „Zachowaj”.

Histogram przed i po operacji:


## 3.6. Zamiana na obraz szaro-odcieniowy

Operacja zamiany obrazu na szaro-odcieniowy dostępna jest w menu Operacje -> Desaturacja.

## 4. Operacje jednopunktowe

## 4.1. Negacja

Operacja negacji jest dostępna w menu Operacje -> Jednopunktowe -> Negacja. Operację należy zatwierdzić w oknie podglądu.


## 4.2. Progowanie

Operacja negacji jest dostępna w menu Operacje -> Jednopunktowe -> Progowanie. Operację należy zatwierdzić w oknie podglądu. Operacja jest wykonywana automatycznie w momencie zmiany jakiejkolwiek z opcji. W oknie Progowania dostępne są opcje:

Progowanie na poziomie 94:


Progowanie na poziomie 94 z zachowaniem poziomów szarości:

Progowanie odwrotne na poziomie 94


## 4.3. Posteryzacja

Operacja posteryzacji dostępna jest w menu Operacje -> Jednopunktowe -> Posteryzacja. W oknie Posteryzacji istnieje możliwość dobrania liczby poziomów szarości, do której sprowadzany jest obraz. Operacja jest wykonywana automatycznie w momencie zmiany poziomów jasności.

Posteryzacja (8 poziomów szarości):


Posteryzacja ( 3 poziomy szarości):

## 4.4. Rozciąganie do zadanych przez użytkownika poziomów jasności

Operacja dostępna jest w menu Operacje -> Jednopunktowe -> Rozciąganie do poziomów jasności. W oknie Rozciągania do poziomów jasności możemy regulować poziomami dolnym i górnym P i Q oraz poziomem poziomu jasności. Operacja jest wykonywana automatycznie w momencie zmiany jakiejkolwiek z opcji.


P w zakresie 0 – 128 , Q w zakresie 120 – 255, poziom tła 0:

P w zakresie 134 – 255 , Q w zakresie 44 – 93 , poziom tła 255 :


## 5. Operacje sąsiedztwa

## 5.1. Wygładzanie liniowe

Operacja dostępna jest w menu Operacje -> Liniowe - > Wygładzanie. W oknie Wygładzania dostępne są opcje wyboru maski, wartości k (dla masek parametryzowanych), operacji na pikseli brzegowych. Operacja jest wykonywana automatycznie w momencie zmiany jakiejkolwiek z opcji. Operację można zwielokrotnić.

Wygładzanie maską [0, 1, 0, 1, 4, 1, 0, 1, 0], piksele brzegowe bez zmian:


Wygładzanie maską [1, 1, 1, 1, k, 1, 1, 1, 1] – k = 2, piksele brzegowe o wartości minimalnej:

## 5.2. Wyostrzanie liniowe

Operacja dostępna jest w menu Operacje -> Liniowe - > Wyostrzanie. W oknie Wyostrzania dostępne są opcje wyboru maski, operacji na pikseli brzegowych, metody skalowania. Operacja jest wykonywana automatycznie w momencie zmiany jakiejkolwiek z opcji. Operację można zwielokrotnić. Naciśnięcie na obraz wyświetli histogram.


Wyostrzanie maską [-1, -1, -1, -1, 9, -1, -1, -1, - 1 ], piksele brzegowe o wartości maksymalnej, skalowanie odcinające:


## 5.3. Detekcja krawędzi

Operacja dostępna jest w menu Operacje -> Liniowe - > Detekcja krawędzi. W oknie Detekcji krawędzi dostępne są opcje wyboru maski, operacji na pikseli brzegowych, metody skalowania. Operacja jest wykonywana automatycznie w momencie zmiany jakiejkolwiek z opcji. Operację można zwielokrotnić. Naciśnięcie na obraz wyświetli histogram.

Detekcja krawędzi maską [0, -1, 0, -1, 4, -1, 0, -1, 0], piksele brzegowe – istniejące sąsiedztwo, metoda skalowania równomierna.


Detekcja krawędzi maską [- 1 , -1, - 1 , -1, 8 , -1, - 1 , -1, - 1 ], piksele brzegowe – powielone, metoda skalowania odcinająca.

## 5.4. Operacja utworzoną przez użytkownika maską

Operacja dostępna jest w menu Operacje -> Liniowe -> Własna maska. W oknie Własnej maski należy wybrać wartości maski 3 x 3 i operację na pikselach brzegowych. Operacja jest wykonywana automatycznie w momencie zmiany jakiejkolwiek z opcji. Operację można zwielokrotnić. Po naciśnięciu na obraz, możemy podejrzeć jego histogram.


Operacja maską [ 1 , 1, 1, 1, -9, 1, 1, 1, 1] z powieleniem pikseli brzegowych:

## 5.5. Złożenie dwóch masek

Operacja dostępna jest w menu Operacje -> Liniowe -> Łączenie masek. W oknie Łączenia masek należy wybrać wartości dwóch masek 3 x 3. Ponadto dostępne jest wybór operacji na pikselach brzegowych i metody skalowania. Operacja jest wykonywana automatycznie w momencie zmiany jakiejkolwiek z opcji. Operacja jest wykonywana automatycznie w momencie zmiany jakiejkolwiek z opcji. Operację można zwielokrotnić. Po naciśnięciu na obraz, możemy podejrzeć jego histogram.


Po prawej stronie okna wyświetlają się wartości wynikowej maski 5x5:

Pomiędzy podglądem między operacją na masce wynikowej 5x5, a dwiema operacjami na maskach wejściowych 3x3, przełączamy się przyciskiem:
(tekst na przycisku mówi, jaki podgląd jest obecnie włączony).

Maska A [1,1,1,1,2,1,1,1,1], maska B [1,1,1,1,-8,1,1,1,1], operacje na maskach składowych:


Te same maski, operacja na masce wynikowej 5x5:

## 5.6. Wygładzanie medianowe

Operacja filtracji medianowej jest dostępna w menu Operacje -> Medianowe - > Filtracja medianowa. W oknie Filtracji medianowej dostępne są opcje wyboru wielkości maski i operacji na pikselach brzegowych. Operacja jest wykonywana automatycznie w momencie zmiany jakiejkolwiek z opcji. Operację można zwielokrotnić. Po naciśnięciu na obraz, możemy podejrzeć jego histogram.


Filtracja medianowa oparta na masce 11x11:

## 5.7. Kierunkowa detekcja krawędzi

## 5.7.1. Filtr Sobela

Wykrywanie krawędzi filtrem kierunkowym Sobela jest dostępne w menu Operacje -> Kierunkowe -> Filtr Sobela. W oknie operacji możemy wybrać kierunek (X, Y lub XY), operację na pikselach brzegowych i metodę skalowania. Dodatkowo dla kierunków X i Y można włączyć filtr Scharra, który w niektórych sytuacjach może dawać lepsze efekty. Operacja jest wykonywana automatycznie w momencie zmiany jakiejkolwiek z opcji. Operację można zwielokrotnić. Po naciśnięciu na obraz, możemy podejrzeć jego histogram.


Filtr Sobela, kierunek X (pionowy):

Kierunek Y (poziomy):

Kierunek XY (ukośny):


## 5.7.2. Filtr Robertsa

Operacja filtrowania kierunkowego metodą Robertsa jest dostępna w menu Operacje > Kierunkowe -> Filtr Robertsa. W oknie operacji możemy określić zakres poziomów jasności. Piksele, których intensywność gradientu znajduje się poniżej wartości dolnej są traktowane jako na pewno nie krawędź i odrzucane. Powyżej poziomu górnego uznane są jako na pewno krawędź. Te znajdujące się w zakresie będą krawędzią lub nie w zależności od tego, w jaki sposób są ze sobą połączone. Dodatkowo możemy zaznaczyć opcję L2 norm, która zmienia sposób liczenia wielkości gradientu i może dać lepsze efekty. Operacja jest wykonywana automatycznie w momencie zmiany jakiejkolwiek z opcji. Operację można zwielokrotnić. Po naciśnięciu na obraz, możemy podejrzeć jego histogram.

Filtr Robertsa (zakres 130 – 177):


## 5.7.3. Filtr Prewitta

Operacja filtrowania kierunkowego metodą Prewitta jest dostępna w menu Operacje > Kierunkowe -> Filtr Prewitta. W oknie operacji możemy określić kierunek X (pionowy) lub Y (poziomy). Operacja jest wykonywana automatycznie w momencie zmiany jakiejkolwiek z opcji. Operację można zwielokrotnić. Po naciśnięciu na obraz, możemy podejrzeć jego histogram.

Kierunek X (pionowy):

Kierunek Y (poziomy):


## 5.8. Operacje morfologiczne

## 5.8.1. Erozja, dylatacja, otwarcie, zamknięcie

Operacje morfologiczne (erozja, dylatacja, otwarcie, zamknięcie) są dostępne w menu Operacje -> Operacje morfologiczne. W oknie operacji możemy wybrać, którą operację chcemy przeprowadzić, jaki kształt i rozmiar ma mieć element strukturalny oraz operację na pikselach brzegowych. Operacja jest wykonywana automatycznie w momencie zmiany jakiejkolwiek z opcji. Operację można zwielokrotnić. Po naciśnięciu na obraz, możemy podejrzeć jego histogram.

Kolejno erozja, dylatacja, otwarcie i zamknięcie kwadratem wielkości 3:
Obraz wejściowy Obraz wyjściowy


Kolejno erozja, dylatacja, otwarcie i zamknięcie rombem wielkości 7 :
Obraz wejściowy Obraz wyjściowy

## 5.8.2. Ścienianie

Operacja ścieniania dostępna jest w menu Operacje -> Ścienianie. W oknie operacji należy wybrać na jakim obiekcie przeprowadzona będzie operacja (czarny obiekt na białym tle, czy biały obiekt na czarnym tle) oraz operację na pikselach brzegowych. Operacja zostanie przeprowadzona po naciśnięciu przycisku Zastosuj. Po wykonaniu ścieniania możemy podejrzeć kolejne kroki algorytmu za pomocą slidera po prawej stronie. Kliknięcie na obraz wyświetli jego histogram.


Wynik operacji:

Podgląd 7. kroku algorytmu:

## 6. Detekcja kształtu

## 6.1. Współczynniki kształtu obrazu

Dla obrazu binarnego istnieje możliwość wyświetlenia jego współczynników kształtu. Jeśli obraz nie jest całkowicie binarny – znajdują się na nim piksele o poziomach jasności innych niż 0 i 255

- zostaną one sprowadzone do poziomu 255.

```
W oknie współczynników kształtu znajduje się podgląd obrazu i – po prawej stronie – zakładki ze współczynnikami podzielonymi na grupy. Należy wybrać, która
grupa nas interesuje i kliknąć na zakładkę, co spowoduje wyświetlenie wyników.
```

Współczynniki kształtu dla koła:


Współczynniki kształtu dla kwadratu:


Bibliografia:

1. Materiały wykładowe do Przetwarzania Obrazów w UBIK.
2. Dokumentacja OpenCV - https://docs.opencv.org/
3. Materiały z przykładami OpenCV - https://opencv-python-tutroals.readthedocs.io/
4. Materiały z przykładami OpenCV - https://www.tutorialspoint.com/
5. Rozwiązania problemów z OpenCV - https://stackoverflow.com/


