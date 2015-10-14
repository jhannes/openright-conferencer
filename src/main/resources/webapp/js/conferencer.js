var ajax = {
  get : function(localUrl) {
    return $.get('/conferencer/' + localUrl);
  },
  post : function(localUrl, object, id) {
    if (id) localUrl += "/" + id;
    return $.ajax({
      url : '/conferencer/' + localUrl,
      type : 'POST',
      data : JSON.stringify(object),
      contentType : "application/json; charset=utf-8"
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
  var _data;
  function get() {
    if (_data) {
      return new Promise(function(resolve) { resolve(data); });
    } else {
      return ajax.get('secure/profile').then(function(data) {
        _data = data;
        return data;
      });
    }
  }
  
  return {
    get: get
  }
})();

