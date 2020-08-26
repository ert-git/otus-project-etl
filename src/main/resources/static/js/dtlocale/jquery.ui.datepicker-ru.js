jQuery(function($){
	$.datepicker.regional['ru'] = {
		closeText: 'Закрыть',
		prevText: '&#x3c;Пред',
		nextText: 'След&#x3e;',
		currentText: 'Сейчас',
		monthNamesShort: ['Янв','Фев','Мар','Апр','Май','Июн','Июл','Авг','Сен','Окт','Ноя','Дек'],
		monthNames: ['Январь','Февраль','Март','Апрель','Май','Июнь',	'Июль','Август','Сентябрь','Октябрь','Ноябрь','Декабрь'],
		dayNamesShort: ['вск','пнд','втр','срд','чтв','птн','сбт'],
		dayNames: ['воскресенье','понедельник','вторник','среда','четверг','пятница','суббота'],
		dayNamesMin: ['Вс','Пн','Вт','Ср','Чт','Пт','Сб'],
		weekHeader: 'Не',
		dateFormat: 'dd.mm.yy',
		timeFormat: 'hh:mm',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: '',
		timeText: 'Время',
		hourText: 'Часы',
		minuteText: 'Мин.',
		secondText: 'Сек.',
		millisecText: 'Милисекунды',
		timezoneText: 'Временная зона',
		timeOnlyTitle: 'Установите время',
		showButtonPanel: true
	};
});