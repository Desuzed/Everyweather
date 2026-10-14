# Телеметрия установки и обновлений

Документ фиксирует безопасный минимум событий для понимания пути установки и
обновления. Событие не должно трактовать источник установки как доказательство
того, какой магазин сейчас может обновить приложение.

## Общие поля

В каждом событии, где поле применимо:

| Поле | Значение |
| --- | --- |
| `install_source` | Нормализованный текущий источник: `google_play`, `galaxy_store`, `app_gallery`, `amazon`, `package_installer`, `other`, `unknown`. ADB и system не выделяются отдельными значениями. |
| `installer_package` | Текущее значение `InstallSourceInfo.getInstallingPackageName()`, без домыслов и без сырых исключений. |
| `install_source_status` | `known`, `unknown`, `error`. `unknown` используется для `null`; `error` — для сбоя запроса источника. |
| `update_channel` | Для Play Core всегда `google_play`. Это канал операции, а не доказательство первоначальной установки. |
| `current_version` | Версия приложения в момент события. |
| `target_version` | Версия, которую Play сообщает для доступного обновления, если она известна. |

При запросе установки сохраняются `origin_install_source` и
`origin_installer_package` как часть маркера ожидающей установки. Они описывают
наблюдавшийся источник в момент запроса, а не неизменяемую историю APK. При
запуске событие `app_install_source` отправляется, если последняя наблюдённая
пара `version + installer_package` изменилась; возврат к старой паре также
создаёт событие.

Android сообщает установщик записи, а не гарантированно первый магазин. На API
30+ используется `getInstallSourceInfo()`. `installingPackageName` может
отличаться от initiating package и быть изменён через
`setInstallerPackageName`; для системной или ADB-установки, а также после
удаления установщика может прийти `null`. Поэтому `unknown` — нормальный
результат, а не ошибка.

## Словарь событий

| Событие | Обязательные данные и смысл |
| --- | --- |
| `app_install_source` | Общие поля. Фиксирует наблюдаемый источник установки/обновления. |
| `in_app_update_check` | `outcome`: `started`, `available`, `unavailable`, `not_allowed`, `downloading`, `downloaded`; разделяет запрос проверки, наличие обновления и состояние текущей загрузки. Ошибка проверки записывается отдельным `in_app_update_error`. |
| `in_app_update_dialog_shown` | `dialog_status`: `ready_to_launch_update` или `ready_to_install`; `target_version` добавляется общими полями, если известна. |
| `in_app_update_dialog_dismissed` | Явное действие `Dismiss` (кнопка/Back); закрытие sheet снаружи или свайпом текущая реализация не отслеживает. Дополнительных полей нет. |
| `in_app_update_start` | Пользователь согласился начать загрузку/flow. |
| `in_app_update_install` | Пользователь согласился установить загруженное обновление. |
| `in_app_update_flow_launched` | Факт, что запуск flow вернул `true`. |
| `in_app_update_consent` | `outcome`: `accepted`, `canceled`, `failed`; `result_code`. `RESULT_CANCELED` означает отказ/отмену, другие неожиданные коды — сбой flow. |
| `in_app_update_download_started` | `resumed=true` при возобновлении; остальные вызовы дедуплицируются по смене состояния listener. |
| `in_app_update_downloaded` | Состояние Play Core `DOWNLOADED`. |
| `in_app_update_download_canceled` | Состояние Play Core `CANCELED`. |
| `in_app_update_install_requested` | Маркер сохраняется до вызова `completeUpdate()` для гибкого flow. |
| `in_app_update_install_canceled` | Состояние Play Core `CANCELED` во время install phase; это отдельный исход от отмены download. |
| `in_app_update_install_accepted` | Успешный `Task<Void>` от `completeUpdate()`; означает принятие запроса SDK, но не факт запуска новой версии. |
| `in_app_update_install_status_installed` | Отправляется только если listener Play Core наблюдал `InstallStatus.INSTALLED`. |
| `in_app_update_version_observed` | `from_version`, `requested_version`, `origin_install_source`, `origin_installer_package`, `matches_target`, `evidence=after_install_request`; следующий запуск обнаружил новую версию, но это не доказывает атрибуцию Play. |
| `in_app_update_install_pending` | Те же marker-поля; на следующем запуске версия та же. Это ожидающее состояние, не ошибка. |
| `in_app_update_install_unconfirmed` | Те же marker-поля и `reason=expired` или `version_rollback`; событие возникает на следующем запуске более чем через 7 дней с запроса либо после rollback. Автоматического фонового таймера нет. |
| `in_app_update_error` | `stage`: `check`, `launch`, `consent`, `download`, `install` или `telemetry_storage`; `error_code`, `error_name` и `exception_type` добавляются только когда доступны. Сырые сообщения исключаются. |
| `settings_launch_update` | Пользователь открыл запуск обновления из настроек; общие поля и `target_version`, если известна. |
| `settings_install_update` | Пользователь открыл prompt установки из настроек; это не подтверждение фактической установки. |

Для всех исходов сохраняется `outcome` в нижнем регистре там, где он предусмотрен
событием. Повторные callbacks загрузки дедуплицируются по смене статуса.

## Наблюдаемое поведение UI

Это поведение подтверждено кодом и локальными fake-SDK тестами; реальные
мультистор-сценарии пока не проверялись на телефоне.

| Событие/исход | Поведение приложения |
| --- | --- |
| Ошибка проверки (`stage=check`) | Prompt обновления не показывается, прогресс очищается/скрывается. Отдельное локализованное сообщение об ошибке не добавляется. |
| Flow не стартовал (`false`) или SDK вернул отмену/ошибку consent | Sheet уже закрыт действием пользователя; прогресс очищается. Ошибка фиксируется телеметрией при наличии кода/исключения. |
| Ошибка загрузки | Прогресс загрузки скрывается, listener отписывается, marker установки очищается. |
| Отмена/ошибка install phase | Для `CANCELED` отправляется `in_app_update_install_canceled`; при `FAILED` или `CANCELED` состояние `ReadyToInstall` сохраняется для retry из настроек, дополнительный локализованный error UI не добавляется. |
| Ошибка `completeUpdate()` | Дополнительный локализованный error UI не добавляется; состояние `ReadyToInstall` сохраняется, чтобы пользователь мог повторить установку из настроек. |
| Результат SDK flow | `RESULT_OK`, `RESULT_CANCELED` и код SDK failure сохраняются в `in_app_update_consent`; окончательная версия подтверждается только следующим запуском. |

## Реальные сценарии Galaxy Store и Google Play

| Сценарий | Ожидаемая телеметрия и вывод |
| --- | --- |
| APK установлен из Galaxy Store, аккаунт уже приобретал приложение в Play, совпадают application ID, подпись и версия ниже Play | `install_source=galaxy_store`, затем Play может вернуть `available`; успешный flow и `version_observed` возможны. Источник сам по себе не означает отказ. |
| APK установлен из Galaxy Store, аккаунт никогда не приобретал приложение в Play | Проверка или установка может завершиться `ERROR_APP_NOT_OWNED` (`-10`). Это означает отсутствие владения Play, а не доказанную причину по одному installer package. |
| APK подписан иначе или Play-версия не выше установленной | Обновление недоступно или установка не проходит; фиксируем наблюдаемый код/стадию, не называем это `APP_NOT_OWNED` без такого кода. Android принимает обновление только при том же application ID, совместимой подписи и не меньшем version code. |
| На устройстве нет официального Play Store | Возможен `ERROR_PLAY_STORE_NOT_FOUND` (`-9`); это отдельный исход от владения приложением. |
| Одинаковая версия после `completeUpdate()` | `in_app_update_install_accepted` может быть успешным, но переход не подтверждён; при следующем запуске отправляется `in_app_update_install_pending`. После запуска более чем через 7 дней — `in_app_update_install_unconfirmed`, если версия так и не изменилась. |

Не следует считать все установки Samsung неуспешными: результат зависит от
аккаунта Play, application ID, подписи, version code, доступности приложения и
состояния устройства. Не следует также считать `RESULT_OK` окончательным
доказательством установленной версии: для flexible update это подтверждение
пользователем запроса; успешный `completeUpdate()` подтверждает только задачу
SDK, а `INSTALLED` — только наблюдение listener. Наиболее сильное подтверждение
— следующая запущенная версия.

## Ручные мультистор-тесты

Эти сценарии требуют контролируемых аккаунтов, одинакового application ID и
подписей, а также версии Play с большим `versionCode`. Реальные установки из
магазинов пока не проверялись; сценарии остаются manual/pending.

1. **Play → Play, положительный:** установить старшую версию из Play поверх
   старшей по условиям тестовой дорожки версии, проверить `available`, flow,
   download/install callbacks и `version_observed` после перезапуска.
2. **Galaxy → Play, eligible positive:** установить APK из Galaxy Store,
   аккаунтом заранее приобрести приложение в Play, обеспечить ту же подпись и
   меньший `versionCode`, затем проверить, что Play flow может завершиться и
   telemetry сохраняет `install_source=galaxy_store` вместе с
   `update_channel=google_play`.
3. **Galaxy → Play, unavailable/error:** повторить с аккаунтом без владения
   Play и/или контролируемой несовместимой подписью; зафиксировать фактический
   `ERROR_APP_NOT_OWNED` либо другой код/стадию, не подменяя его диагнозом по
   источнику установки.

## Ошибки и сбор данных

Нормализуем как минимум следующие коды Play Core: `-10`
(`ERROR_APP_NOT_OWNED`), `-9` (`ERROR_PLAY_STORE_NOT_FOUND`), `-6`
(`ERROR_INSTALL_NOT_ALLOWED`), `-5` (`ERROR_INSTALL_UNAVAILABLE`), `-4`
(`ERROR_INVALID_REQUEST`), `-3` (`ERROR_API_NOT_AVAILABLE`), `-2`
(`ERROR_UNKNOWN`) и `-100` (`ERROR_INTERNAL_ERROR`). Коды сохраняются вместе со
стадией, потому что один и тот же пользовательский симптом может возникнуть на
разных этапах. Причина вроде «установлено из Galaxy Store» допускается только
как контекст `install_source`, не как диагноз.

Для Firebase release-сборки телеметрия включена, для debug-сборки выключена.
Код не регистрирует custom dimensions автоматически: если нужны отчёты по
`install_source`, `update_channel`, `outcome`, `stage`, `error_name` или
`dialog_status`, их нужно отдельно настроить в Firebase/GA4. Сырые исключения,
сообщения и ключи в параметры не передаются.

## Архитектурные правила реализации

Контракт обновлений хранится в `UpdateAnalyticsContract.kt`: там находятся
константы событий, имён параметров и допустимых значений, включая вложенные
`Sources`, `SourceStatus`, имена ошибок и пакеты установщиков. Типизированные
`UpdateStage`, `UpdateCheckOutcome` и `UpdateConsentOutcome` ограничивают значения на
границах API; в Firebase уходят их стабильные строковые представления.

`UpdateAnalytics.kt` — единый фасад с переиспользуемыми методами записи
проверки, диалога, согласия, загрузки, установки и ошибок. Recorder обогащает
события общими метаданными и сохраняет маркер ожидающей установки без изменения
существующей persistence-логики. Словарь payload и смысл существующих событий
сохраняются; добавление общих метаданных не меняет их триггеры.

Ошибки представлены сущностью `UpdateAnalyticsError.kt`, которая передаёт
стабильные имя, стадию и числовой код SDK без сырых исключений и их сообщений.
Код `-8` вынесен в именованную константу совместимости: он описан в справочнике
Play, но не экспортируется используемой библиотекой `app-update 2.1.0`.

Существующие вызовы аналитики остаются совместимыми: `Analytics.logEvent`
сохраняет прежний API, а `parameters` по умолчанию равен пустой карте. Старые
события и их пользовательские триггеры не переименовываются и не дублируются;
остальная аналитика приложения продолжает работать без изменений.

## Бесплатная настройка Firebase и GA4

Проект можно использовать на бесплатном плане Firebase Spark: Firebase
Analytics предоставляется без оплаты, и для начала сбора Analytics не нужен
способ оплаты. Для этой телеметрии не требуется переход на Blaze, Analytics
360, BigQuery или Cloud Functions. Фактический тарифный план проекта этим
документом не проверяется.

Порядок настройки:

1. В Firebase Console откройте проект и Analytics; свяжите его с ресурсом
   Google Analytics, если связь ещё не создана.
2. В связанном ресурсе GA4 откройте **Admin → Custom definitions → Create
   custom dimension**. Для каждой dimension выберите scope **Event** и
   зарегистрируйте параметры `install_source`, `update_channel`, `outcome`,
   `stage`, `error_name` и `dialog_status`. При необходимости можно добавить
   `error_code`.
3. Нужны права Editor или Administrator. Имена событий SDK регистрирует при
   отправке автоматически: вручную создавать event definitions для этих
   имён не нужно, а прежние имена и отчёты сохраняются.

После отправки параметров и создания custom definitions данные обычно
становятся доступны через 24–48 часов. В стандартном GA4 действует лимит до
50 event-scoped custom dimensions на ресурс; регистрируйте только нужные
параметры.

Ссылки: [Firebase pricing](https://firebase.google.com/pricing),
[Firebase Analytics](https://firebase.google.com/docs/analytics),
[GA4 custom dimensions](https://support.google.com/analytics/answer/14239696?hl=en)
и [регистрация custom dimensions](https://support.google.com/analytics/answer/14240153?hl=en).

## Источники

- [InstallSourceInfo](https://developer.android.com/reference/android/content/pm/InstallSourceInfo) и [PackageManager.getInstallSourceInfo](https://developer.android.com/reference/android/content/pm/PackageManager#getInstallSourceInfo(java.lang.String)).
- [How app updates work](https://developer.android.com/google/play/app-updates).
- [Test in-app updates](https://developer.android.com/guide/playcore/in-app-updates/test).
- [AppUpdateManager](https://developer.android.com/reference/com/google/android/play/core/appupdate/AppUpdateManager).
- [InstallErrorCode](https://developer.android.com/reference/com/google/android/play/core/install/model/InstallErrorCode), [InstallState](https://developer.android.com/reference/com/google/android/play/core/install/InstallState) и [ActivityResult](https://developer.android.com/reference/com/google/android/play/core/install/model/ActivityResult).
