(function() {
    'use strict';

    angular
        .module('backendApp')
        .config(bootstrapMaterialDesignConfig);

    //compileServiceConfig.$inject = [];

    function bootstrapMaterialDesignConfig() {
        $.material.init();

    }
})();
