(function() {
    'use strict';

    angular
        .module('backendApp')
        .controller('EventImageDeleteController',EventImageDeleteController);

    EventImageDeleteController.$inject = ['$uibModalInstance', 'entity', 'EventImage'];

    function EventImageDeleteController($uibModalInstance, entity, EventImage) {
        var vm = this;

        vm.eventImage = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            EventImage.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
