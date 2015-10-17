var ajax = {
  get : function(localUrl) {
    $("#loading-overlay").fadeIn();
    return $.get('/conferencer/' + localUrl).always(function() {
      $("#loading-overlay").fadeOut();  
    });
  },
  put: function(localUrl, object) {
    $("#loading-overlay").fadeIn();
    return $.ajax({
      url : '/conferencer/' + localUrl,
      type : 'PUT',
      data : JSON.stringify(object),
      contentType : "application/json; charset=utf-8"
    }).always(function() {
      $("#loading-overlay").fadeOut();  
    });    
  },
  post : function(localUrl, object, id) {
    if (id) localUrl += "/" + id;
    $("#loading-overlay").fadeIn();
    return $.ajax({
      url : '/conferencer/' + localUrl,
      type : 'POST',
      data : JSON.stringify(object),
      contentType : "application/json; charset=utf-8"
    }).always(function() {
      $("#loading-overlay").fadeOut();  
    });
  }
};

$(document).ajaxError(function( event, jqxhr, settings, thrownError) {
  console.log( event, jqxhr, settings, thrownError);
  if (jqxhr.status >= 500) {
    notify("error", "A terrible error occurred", "We are very sorry and looking into it");
  } else if (jqxhr.status == 401) {
    window.location = "login.html";
  } else if (jqxhr.status == 404) {
    notify("warning", "Not found", thrownError);
  } else if (jqxhr.status >= 400) {
    notify("warning", "Warning", thrownError);
  } else {
    notify("warning", "Problems", thrownError);
  }
});



window.Handlebars.registerHelper('select', function( value, options ){
  var $el = $('<select />').html( options.fn(this) );
  $el.find('[value=' + value + ']').attr({'selected':'selected'});
  return $el.html();
});


var orderRepository = {
  get: function(id) {
    return ajax.get('api/orders/' + id);
  },

  list: function() {
    return ajax.get('api/orders');
  },

  save: function(order) {
    return ajax.post('api/orders', order, order.id);
  }
};

var productRepository = {
  get: function(id) {
    return ajax.get('api/products/' + id);
  },

  list: function() {
    return ajax.get('api/products');
  },

  save: function(product) {
    return ajax.post('api/products', product, product.id);
  }
};

var profile = (function() {
  function get() {
    return ajax.get('secure/profile').then(function(data) {
      _data = data;
      return data;
    });
  }
  
  return {
    get: get
  }
})();

var events = (function() {
  function get(slug) {
    return ajax.get('secure/events/' + slug);
  }
  
  function save(data) {
    return ajax.post('secure/events', data);
  }
  
  function update(data) {
    return ajax.put('secure/events/' + data.event.slug, data);
  }
  
  return {
    save: save,
    get: get,
    update: update
  };
})();