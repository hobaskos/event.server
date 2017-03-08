(function() {
    'use strict';

    angular
        .module('backendApp')
        .controller('EventPollDeleteController',EventPollDeleteController);

    EventPollDeleteController.$inject = ['$uibModalInstance', 'entity', 'EventPoll'];

    function EventPollDeleteController($uibModalInstance, entity, EventPoll) {
        var vm = this;

        vm.eventPoll = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            EventPoll.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
