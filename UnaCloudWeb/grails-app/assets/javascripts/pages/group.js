$('.delete_group').click(function (event){	
	event.preventDefault();
	var data = $(this).data("id");
	var href = $(this).attr("href");
	showConfirm('Confirm','This Group will be deleted. Are you sure you want to continue?', function(){		
		window.location.href = href+data;
	});
});