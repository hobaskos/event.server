(function() {
    'use strict';

    angular
        .module('backendApp')
        .factory('EventImageSearch', EventImageSearch);

    EventImageSearch.$inject = ['$resource'];

    function EventImageSearch($resource) {
        var resourceUrl =  'api/_search/event-images/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true}
        });
    }
})();
